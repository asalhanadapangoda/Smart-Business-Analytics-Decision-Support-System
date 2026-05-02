@echo off
python -m uvicorn app.main:app --reload --port 8000
if %ERRORLEVEL% NEQ 0 (
    echo Python not found or uvicorn failed. Trying "python3"...
    python3 -m uvicorn app.main:app --reload --port 8000
)
pause
