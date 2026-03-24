from gevent import monkey
monkey.patch_all()

import uuid
from gevent.lock import BoundedSemaphore
from locust import HttpUser, between, task

_counter_lock = BoundedSemaphore(1)
_counter = 0

def next_user_index():
    global _counter
    with _counter_lock:
        _counter += 1
        return _counter

class WebsiteUser(HttpUser):
    wait_time = between(5, 15)
    account_id = None
    user_id = None
    token = None

    def on_start(self):
        user_index = next_user_index()
        if not self._create_user(user_index):
            self.environment.runner.quit()
            return
        if not self._authenticate(user_index):
            self.environment.runner.quit()
            return
        if not self._fetch_user_id():
            self.environment.runner.quit()
            return
        self._create_account()

    def _headers(self):
        return {"Authorization": f"Bearer {self.token}"}

    def _create_user(self, user_index: int) -> bool:
        unique_id = uuid.uuid4().hex[:8]
        self.email = f"test_user_{user_index}_{unique_id}@email.com"
        self.name = f"test_user_{user_index}_{unique_id}"
        response = self.client.post("/auth/register", json={
            "name": self.name,
            "email": self.email,
            "password": "1234"
        })
        if not response or response.status_code not in (200, 201):
            print(f"[ERRO] Register falhou: {response.status_code} - {response.text}")
            return False
        return True

    def _authenticate(self, user_index: int) -> bool:
        response = self.client.post("/auth/login", json={
            "email": self.email,
            "password": "1234"
        })
        if not response or response.status_code != 200:
            print(f"[ERRO] Login falhou: {response.status_code} - {response.text}")
            return False
        self.token = response.json().get("token")
        return True

    def _fetch_user_id(self) -> bool:
        me = self.client.get("/users/me", headers=self._headers())
        if not me or me.status_code != 200:
            print(f"[ERRO] /users/me falhou: {me.status_code} - {me.text}")
            return False
        self.user_id = me.json().get("id")
        return True

    def _create_account(self):
        response = self.client.post("/accounts", json=self._account_payload(), headers=self._headers())
        if not response or response.status_code != 201:
            print(f"[ERRO] /accounts falhou: {response.status_code} - {response.text}")
            return
        self.account_id = response.json().get("id")

    def _account_payload(self):
        return {
            "bankName": "Banco Teste",
            "accountType": "Corrente",
            "balance": 10000.00,
            "user": {"id": self.user_id}
        }

    @task
    def create_account(self):
        if not self.token or not self.user_id:
            return
        response = self.client.post("/accounts", json=self._account_payload(), headers=self._headers())
        if not response or response.status_code != 201:
            print(f"[ERRO] create_account falhou: {response.status_code} - {response.text}")
            return
        self.account_id = response.json().get("id")

    @task
    def get_account_by_id(self):
        if not self.account_id or not self.token:
            return
        self.client.get(f"/accounts/{self.account_id}", headers=self._headers())