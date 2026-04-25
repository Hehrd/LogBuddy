from fastapi import FastAPI, Request


app = FastAPI(title="Local Hook Server")


@app.post("/hook")
async def receive_hook(request: Request) -> dict[str, str]:
    payload = await request.json()
    print(payload)
    return {"status": "received"}
