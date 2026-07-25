from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import streamlink
from typing import Optional
app = FastAPI()
class CreaTVRequest(BaseModel):
    platform: Optional[str] = None
    creator: Optional[str] = None
    direct_url: Optional[str] = None

@app.post("/stream-link")
def get_stream_url(data: CreaTVRequest):
    target_url = ""

    if data.direct_url:
        target_url = data.direct_url
    elif data.platform and data.creator:
        plat = data.platform.lower()
        if plat == "twitch":
            target_url = f"https://www.twitch.tv/{data.creator}"
        elif plat == "kick":
            target_url = f"https://kick.com/{data.creator}"
        elif plat == "youtube":
            target_url = f"https://www.youtube.com/@{data.creator}/live"
        else:
            raise HTTPException(status_code=400, detail="Error: Platform no found")
    else:
        raise HTTPException(
            status_code=400, 
            detail="Invalid data!"
        )

    
