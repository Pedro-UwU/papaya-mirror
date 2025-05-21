import logging
from dataclasses import dataclass
from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
from typing import Optional
from uuid import uuid4

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(message)s")
logger = logging.getLogger("simple_api")

app = FastAPI()

users = {}
friends = {}


@dataclass
class CreateUserRequest:
    name: str
    username: str
    password: str
    email: str
    age: int
    surname: Optional[str]


@dataclass
class FriendInfo:
    name: str
    id: str


@dataclass
class AddFriendsRequest:
    id: str
    friends: list[FriendInfo]


@app.post("/user")
def create_user(data: CreateUserRequest):
    user_id = str(uuid4())
    users[user_id] = data

    headers = {
        "X-Cat-Dog": "alone in the world",
        "Content-Language": "es",
        "Authorization": f"Bearer {uuid4()}",
    }  # From FastAPI docs
    content = {"user_id": user_id}

    return JSONResponse(content=content, headers=headers)


@app.post("/friend")
def add_friend(data: AddFriendsRequest):
    user_id = data.id
    if user_id not in users:
        raise HTTPException(status_code=404, detail="User not found")
    if user_id not in friends:
        friends[user_id] = list()
    for friend in data.friends:
        friends[user_id].append(friend)


@app.get("/user")
def get_user(user_id: str):
    if user_id not in users:
        raise HTTPException(status_code=404, detail="User not found")

    return users[user_id]


@app.get("/user/all")
def get_all_user():
    return users


@app.patch("/user/password")
def update_password(user_id: str, old_password: str, new_password: str):
    if user_id not in users:
        raise HTTPException(status_code=404, detail="User not found")

    if users[user_id].password != old_password:
        print(f"User {users[user_id]}, Old Pass {old_password}")
        raise HTTPException(status_code=403, detail="Incorrect password")

    users[user_id].password = new_password
