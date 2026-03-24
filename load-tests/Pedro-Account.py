from locust import HttpUser, between, task
from itertools import count

user_counter = count(1)

class WebsiteUser(HttpUser):
    wait_time = between(5, 15)
    account_id = None
    user_id = None

    def on_start(self):
        user_index = next(user_counter)
        email = f"test_user_{user_index}@email.com"

        self.client.post("/auth/register", json={
            "name": f"test_user_{user_index}",
            "email": email,
            "password": "1234"
        })

        response = self.client.post("/auth/login", json={
            "email": email,
            "password": "1234"
        })
        self.token = response.json().get("token")

        me = self.client.get("/users/me", headers=self._headers())
        self.user_id = me.json().get("id")

        create_response = self.client.post("/accounts", json={
            "bankName": "Banco Teste",
            "accountType": "Corrente",
            "balance": 10000.00,
            "user": {"id": self.user_id}
        }, headers=self._headers())
        self.account_id = create_response.json().get("id")

    def _headers(self):
        return {"Authorization": f"Bearer {self.token}"}

    @task
    def create_account(self):
        create_response = self.client.post("/accounts", json={
            "bankName": "Banco Teste",
            "accountType": "Corrente",
            "balance": 10000.00,
            "user": {"id": self.user_id}
        }, headers=self._headers())
        self.account_id = create_response.json().get("id")

    @task
    def get_account_by_id(self):
        if self.account_id is None:
            return
        self.client.get(f"/accounts/{self.account_id}", headers=self._headers())