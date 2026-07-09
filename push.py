import subprocess
import os

os.chdir(r"C:\Users\scroam\Desktop\scroambase")

git_path = r"D:\Program Files\Git\bin\git.exe"

cmds = [
    ["add", "."],
    ["commit", "-m", "Add login system with registration, authentication and session management"],
    ["push", "-u", "origin", "main"]
]

for cmd in cmds:
    print(f"=== git {' '.join(cmd)} ===")
    result = subprocess.run([git_path] + cmd, capture_output=True, text=True, timeout=60)
    print("OUT:", result.stdout)
    print("ERR:", result.stderr)
    print("RC:", result.returncode)
    print()