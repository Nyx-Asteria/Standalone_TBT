"""Exercise the real session exporter with a simulated USB mount."""
import os
from pathlib import Path
import subprocess
import tempfile

root = Path(__file__).resolve().parents[1]
with tempfile.TemporaryDirectory(prefix="session-export-") as tmp:
    p = Path(tmp)
    source = p / "source"
    source.mkdir()
    focused = p / "focused"
    focused.mkdir()
    (focused / "session-waze.log").write_text("START Waze\nEND\n")
    (focused / "session-mapquest.active").write_text("START MapQuest\n")
    usb = p / "storage" / "usb0"
    usb.mkdir(parents=True)
    mounts = p / "mounts"
    mounts.write_text("")
    (source / "session-100.log").write_text("START\nEND\n")
    (source / "session-101.active").write_text("START\n")
    (source / "session-102-interrupted.log").write_text("START\nEND interrupted\n")
    dest = usb / "TBT-LOGS" / "sessions"
    script = (root / "export-sessions.sh").read_text()
    script = script.replace("/data/data/com.daudio.app.dipo/files/stbt-carplay-sessions", str(source))
    script = script.replace("/data/data/com.daudio.app.dipo/files/stbt-third-party-navigation", str(focused))
    focused_dest = usb / "TBT-LOGS" / "third-party-navigation"
    script = script.replace("/storage/usb", str(p / "storage" / "usb"))
    script = script.replace("/proc/mounts", str(mounts))
    script = script.replace("    sleep 5\n", "    break\n")
    assert "    break\n" in script
    def run():
        subprocess.run(["sh", "-c", script], check=True, timeout=10)
    run()
    assert not focused_dest.exists()
    assert not dest.exists(), "unmounted path must not receive files"
    mounts.write_text("device " + str(usb) + " vfat rw 0 0\n")
    run()
    assert (dest / "session-100.log").read_bytes() == (source / "session-100.log").read_bytes()
    assert (dest / "session-102-interrupted.log").exists()
    assert not (dest / "session-101.active").exists()
    assert (source / "session-100.log").exists()
    assert not list(dest.glob("*.part"))
    (source / "session-101.active").rename(source / "session-101.log")
    run()
    assert (dest / "session-101.log").exists()
    assert len(list(dest.glob("*.log"))) == 3
    assert (focused_dest / "session-waze.log").read_bytes() == (focused / "session-waze.log").read_bytes()
    assert not (focused_dest / "session-mapquest.active").exists()
    assert not (dest / "session-waze.log").exists()
    assert not list(focused_dest.glob("*.part"))
    (focused / "session-mapquest.active").rename(focused / "session-mapquest.log")
    run()
    assert (focused_dest / "session-mapquest.log").exists()
    print("Session export: mount guard, delayed USB, completed-only, recovery files, retained source and repeat export passed")
