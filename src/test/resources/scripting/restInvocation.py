import requests
r = requests.get('https://jenapp.appgallabs.io/microservice/')
print(r.status_code)
print(r.json())