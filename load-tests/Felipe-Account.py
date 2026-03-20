from gevent import monkey
monkey.patch_all()

from locust import HttpUser, between, task
from itertools import count

user_counter = count(1)

class WebsiteUser(HttpUser):
    wait_time = between(5, 15)
    account_id = None
    user_id = None
    token = None

    def on_start(self):
        user_index = next(user_counter)
        self._create_user(user_index)
        self._authenticate(user_index)
        self._fetch_user_id()
        self._create_account()

    def _headers(self):
        return {"Authorization": f"Bearer {self.token}"}

    def _create_user(self, user_index: int):
        self.email = f"test_user_{user_index}@email.com"
        self.name = f"test_user_{user_index}"
        self.client.post("/auth/register", json={
            "name": self.name,
            "email": self.email,
            "password": "1234"
        })

    def _authenticate(self, user_index: int):
        response = self.client.post("/auth/login", json={
            "email": self.email,
            "password": "1234"
        })
        if not response or response.status_code != 200:
            print(f"[ERRO] Login falhou: {response.status_code} - {response.text}")
            self.token = None
            return
        self.token = response.json().get("token")

    def _fetch_user_id(self):
        me = self.client.get("/users/me", headers=self._headers())
        if not me or me.status_code != 200:
            print(f"[ERRO] /users/me falhou: {me.status_code} - {me.text}")
            self.user_id = None
            return
        self.user_id = me.json().get("id")

    def _create_account(self):
        response = self.client.post("/accounts", json=self._account_payload(), headers=self._headers())
        if not response or response.status_code != 201:
            print(f"[ERRO] /accounts falhou: {response.status_code} - {response.text}")
            self.account_id = None
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
        response = self.client.post("/accounts", json=self._account_payload(), headers=self._headers())
        if not response or response.status_code != 201:
            print(f"[ERRO] create_account falhou: {response.status_code} - {response.text}")
            return
        self.account_id = response.json().get("id")

    @task
    def get_account_by_id(self):
        if not self.account_id:
            return
        self.client.get(f"/accounts/{self.account_id}", headers=self._headers())