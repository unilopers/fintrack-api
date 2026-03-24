import random
from uuid import uuid4

from locust import HttpUser, between, task


BANK_NAMES = ["Nubank", "Itau", "Bradesco", "Banco do Brasil", "Santander", "Inter", "C6 Bank"]
ACCOUNT_TYPES = ["Corrente", "Poupanca"]
PASSWORD = "LoadTest@123"


class AccountLoadTest(HttpUser):
    wait_time = between(1, 3)

    def on_start(self):
        self.token = None
        self.account_ids = []

        unique_id = uuid4().hex[:12]
        self.email = f"loadtest-{unique_id}@test.com"
        self.name = f"LoadUser {unique_id}"

        if not self._register():
            return
        if not self._login():
            return

    def _register(self):
        with self.client.post(
            "/auth/register",
            json={"name": self.name, "email": self.email, "password": PASSWORD},
            catch_response=True,
        ) as response:
            if response.status_code == 201:
                response.success()
                return True
            response.failure(f"Register failed: {response.status_code} - {response.text}")
            self.environment.runner.quit()
            return False

    def _login(self):
        with self.client.post(
            "/auth/login",
            json={"email": self.email, "password": PASSWORD},
            catch_response=True,
        ) as response:
            if response.status_code != 200:
                response.failure(f"Login failed: {response.status_code} - {response.text}")
                self.environment.runner.quit()
                return False

            try:
                data = response.json()
                self.token = data.get("token")
            except Exception:
                response.failure("Login response is not valid JSON")
                self.environment.runner.quit()
                return False

            if not self.token:
                response.failure("Login response missing token")
                self.environment.runner.quit()
                return False

            response.success()
            return True

    def _headers(self):
        return {"Authorization": f"Bearer {self.token}"}

    @task(3)
    def create_account(self):
        if not self.token:
            return

        payload = {
            "bankName": random.choice(BANK_NAMES),
            "accountType": random.choice(ACCOUNT_TYPES),
            "balance": round(random.uniform(100.0, 50000.0), 2),
        }

        with self.client.post(
            "/accounts",
            json=payload,
            headers=self._headers(),
            catch_response=True,
        ) as response:
            if response.status_code == 201:
                try:
                    data = response.json()
                    account_id = data.get("id")
                    if account_id:
                        self.account_ids.append(account_id)
                    response.success()
                except Exception:
                    response.failure("Create account: response is not valid JSON")
            else:
                response.failure(f"Create account failed: {response.status_code} - {response.text}")

    @task(2)
    def get_account_by_id(self):
        if not self.token or not self.account_ids:
            return

        account_id = random.choice(self.account_ids)

        with self.client.get(
            f"/accounts/{account_id}",
            headers=self._headers(),
            catch_response=True,
        ) as response:
            if response.status_code == 200:
                try:
                    response.json()
                    response.success()
                except Exception:
                    response.failure("Get account: response is not valid JSON")
            else:
                response.failure(f"Get account failed: {response.status_code} - {response.text}")

    @task(1)
    def get_all_accounts(self):
        if not self.token:
            return

        with self.client.get(
            "/accounts",
            headers=self._headers(),
            catch_response=True,
        ) as response:
            if response.status_code == 200:
                try:
                    response.json()
                    response.success()
                except Exception:
                    response.failure("Get all accounts: response is not valid JSON")
            else:
                response.failure(f"Get all accounts failed: {response.status_code} - {response.text}")