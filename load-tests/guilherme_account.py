from locust import HttpUser, between, task
from uuid import uuid4

class AccountLoadUser(HttpUser):
    wait_time = between(1, 5)

    account_id = None
    user_id = None
    token = None

    def on_start(self):
        unique_id = uuid4().hex
        email = f"load_user_{unique_id}@teste.com"

        register_response = self.client.post("/auth/register", json={
            "name": f"load_user_{unique_id}",
            "email": email,
            "password": "senha123"
        })
        if register_response.status_code != 201:
            self.token = None
            return

        login_response = self.client.post("/auth/login", json={
            "email": email,
            "password": "senha123"
        })
        if login_response.status_code != 200:
            self.token = None
            return

        self.token = login_response.json().get("token")

        me_response = self.client.get("/users/me", headers=self._headers())
        if me_response.status_code != 200:
            self.token = None
            return

        self.user_id = me_response.json().get("id")

    def _headers(self):
        return {"Authorization": f"Bearer {self.token}"}

    @task(2)
    def create_account(self):
        if self.token is None or self.user_id is None:
            return

        response = self.client.post("/accounts", json={
            "bankName": "Banco Carga",
            "accountType": "Poupança",
            "balance": 500.00,
            "user": {"id": self.user_id}
        }, headers=self._headers())

        if response.status_code == 201:
            self.account_id = response.json().get("id")

    @task(1)
    def get_account_by_id(self):
        if self.token is None or self.account_id is None:
            return

        self.client.get(f"/accounts/{self.account_id}", headers=self._headers())
