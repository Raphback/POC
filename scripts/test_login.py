import requests
import json

url = "http://localhost:8080/api/auth/login"
headers = {'Content-Type': 'application/json'}

# Test Student Login (INE)
payload = {"matricule": "120890177FA"}

try:
    response = requests.post(url, headers=headers, json=payload)
    print(f"Status Code: {response.status_code}")
    print(f"Response: {response.text}")
    
    if response.status_code == 200:
        print("✅ Login Successful")
    else:
        print("❌ Login Failed")
except Exception as e:
    print(f"❌ Connection Error: {e}")

# Test Admin Login
print("\nTesting Admin Login...")
admin_payload = {"username": "admin", "password": "admin"}
try:
    response = requests.post("http://localhost:8080/api/auth/login/admin", headers=headers, json=admin_payload)
    print(f"Status Code: {response.status_code}")
    if response.status_code == 200:
         print("✅ Admin Login Successful")
         token = response.json().get('token')
         # Verify student count
         print("Checking student count...")
         # Assuming there is an endpoint for students, but let's just rely on login for now.
         # Actually let's try to get /api/etudiants if possible
         students_resp = requests.get("http://localhost:8080/api/etudiants", headers={"Authorization": f"Bearer {token}"})
         if students_resp.status_code == 200:
             print(f"Students found: {len(students_resp.json())}")
         else:
             print(f"Failed to get students: {students_resp.status_code}")
    else:
         print("❌ Admin Login Failed")
except Exception as e:
    print(f"❌ Admin Error: {e}")
