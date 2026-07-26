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

    try:
        session = streamlink.Streamlink()
        session.set_option("http-headers", {
            "User-Agent": "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Mobile Safari/537.36"
        })
        
        streams = session.streams(target_url)

        if not streams:
            raise HTTPException(
                status_code=404, 
                detail=f"No Streams for {target_url}"
            )

        result = {
            "link_best": streams["best"].url if "best" in streams else None,
            "link_worst": streams["worst"].url if "worst" in streams else None,
        }
        
        return result

    except streamlink.exceptions.NoPluginError:
        raise HTTPException(
            status_code=400, 
            detail="Streamlink no found plugin"
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error process stream: {str(e)}")
    
