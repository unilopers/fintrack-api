from locust import HttpUser, between, task, constant_pacing
from itertools import count

user_counter = count(1)

class AccountLoadUser(HttpUser):
    wait_time = between(1, 5)

    account_id = None
    user_id = None
    token = None

    def on_start(self):
        user_index = next(user_counter)
        email = f"load_user_{user_index}@teste.com"

        self.client.post("/auth/register", json={
            "name": f"load_user_{user_index}",
            "email": email,
            "password": "senha123"
        })

        response = self.client.post("/auth/login", json={
            "email": email,
            "password": "senha123"
        })

        self.token = response.json().get("token")

        me = self.client.get("/users/me", headers=self._headers())
        self.user_id = me.json().get("id")

    def _headers(self):
        return {"Authorization": f"Bearer {self.token}"}

    @task(2)
    def create_account(self):
        response = self.client.post("/accounts", json={
            "bankName": "Banco Carga",
            "accountType": "Poupança",
            "balance": 500.00,
            "user": {"id": self.user_id}
        }, headers=self._headers())

        data = response.json()
        if data.get("id"):
            self.account_id = data.get("id")

    @task(1)
    def get_account_by_id(self):
        if self.account_id is None:
            return
        self.client.get(f"/accounts/{self.account_id}", headers=self._headers())
