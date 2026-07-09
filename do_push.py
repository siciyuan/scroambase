import subprocess
import os

os.chdir(r"C:\Users\scroam\Desktop\scroambase")

git_path = r"D:\Program Files\Git\bin\git.exe"

print("=== Pushing to GitHub ===")
result = subprocess.run([git_path, "push", "-u", "origin", "main"], capture_output=True, text=True, timeout=60)
print("STDOUT:", result.stdout)
print("STDERR:", result.stderr)
print("Exit code:", result.returncode)