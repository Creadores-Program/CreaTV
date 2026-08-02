from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import streamlink
from streamlink.exceptions import StreamlinkError, PluginError, NoPluginError
import yt_dlp
from typing import Optional

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

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
            raise HTTPException(status_code=400, detail="Error: Platform not found")
    else:
        raise HTTPException(
            status_code=400, 
            detail="Invalid data!"
        )

    try:
        session = streamlink.Streamlink()
        session.set_option("http-headers", {
            "User-Agent": "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Mobile Safari/537.36"
        })

        if "youtube" in target_url or "youtu.be" in target_url:
            ydl_opts = {'quiet': True, 'format': 'best'}
            with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                info = ydl.extract_info(target_url, download=False)
                hls_url = info['url'] or info.get('manifest_url')
                if not hls_url and 'formats' in info:
                    for f in info['formats']:
                        if f.get('ext') == 'm3u8' or 'm3u8' in f.get('url', ''):
                            hls_url = f['url']
                            break
                
                if not hls_url:
                    raise HTTPException(
                        status_code=404,
                        detail=f"No Streams available for {target_url}"
                    )

            target_url = f"hls://{hls_url}"
        
        streams = session.streams(target_url)

        if not streams:
            raise HTTPException(
                status_code=404, 
                detail=f"No Streams available for {target_url}"
            )

        result = {
            "link_best": streams["best"].url if "best" in streams else None,
            "link_worst": streams["worst"].url if "worst" in streams else None,
        }
        
        return result

    except NoPluginError:
        raise HTTPException(
            status_code=400, 
            detail="No streamlink plugin found for this URL"
        )
    except (PluginError, StreamlinkError) as e:
        raise HTTPException(
            status_code=404, 
            detail=f"Stream offline or unreachable: {str(e)}"
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Unexpected error: {str(e)}")
    
