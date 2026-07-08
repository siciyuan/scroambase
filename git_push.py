import subprocess
import os

os.chdir(r"C:\Users\scroam\Desktop\scroambase")

git_path = r"D:\Program Files\Git\bin\git.exe"

commands = [
    ["config", "--global", "user.name", "siciyuan"],
    ["config", "--global", "user.email", "siciyuan@users.noreply.github.com"],
    ["add", "."],
    ["commit", "-m", "Initial commit: ScroamDB Economy Plugin"],
    ["remote", "add", "origin", "https://github.com/siciyuan/scroambase.git"],
    ["branch", "-M", "main"],
    ["push", "-u", "origin", "main"]
]

for cmd in commands:
    print(f"=== Executing: git {' '.join(cmd)} ===")
    try:
        result = subprocess.run([git_path] + cmd, capture_output=True, text=True, timeout=60)
        if result.stdout:
            print("STDOUT:", result.stdout)
        if result.stderr:
            print("STDERR:", result.stderr)
        print(f"Exit code: {result.returncode}")
    except subprocess.TimeoutExpired:
        print("Command timed out")
    print()