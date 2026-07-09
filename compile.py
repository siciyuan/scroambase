import subprocess
import os

os.chdir(r"C:\Users\scroam\Desktop\scroambase")

javac = r"C:\Users\scroam\Downloads\MSL\Java\21\bin\javac.exe"
libs = [
    r"C:\Users\scroam\Downloads\MSL\Server\libraries\io\papermc\paper\paper-api\1.21.11-R0.1-SNAPSHOT\paper-api-1.21.11-R0.1-SNAPSHOT.jar",
    r"C:\Users\scroam\Downloads\MSL\Server\libraries\net\kyori\adventure-api\4.26.1\adventure-api-4.26.1.jar",
    r"C:\Users\scroam\Downloads\MSL\Server\libraries\net\kyori\adventure-key\4.26.1\adventure-key-4.26.1.jar",
    r"C:\Users\scroam\Downloads\MSL\Server\libraries\net\kyori\adventure-text-minimessage\4.26.1\adventure-text-minimessage-4.26.1.jar",
    r"C:\Users\scroam\Downloads\MSL\Server\libraries\net\kyori\adventure-text-serializer-legacy\4.26.1\adventure-text-serializer-legacy-4.26.1.jar",
    r"C:\Users\scroam\Downloads\MSL\Server\libraries\net\kyori\adventure-text-serializer-gson\4.26.1\adventure-text-serializer-gson-4.26.1.jar",
    r"C:\Users\scroam\Downloads\MSL\Server\libraries\net\md-5\bungeecord-chat\1.21-R0.2-deprecated+build.21\bungeecord-chat-1.21-R0.2-deprecated+build.21.jar",
    r"C:\Users\scroam\Downloads\MSL\Server\libraries\com\google\code\gson\gson\2.13.2\gson-2.13.2.jar",
    r"C:\Users\scroam\Downloads\MSL\Server\libraries\org\sqlite\sqlite-jdbc\3.49.1.0\sqlite-jdbc-3.49.1.0.jar"
]
cp = ";".join(libs)
srcDir = "src\\main\\java"
outDir = "target\\classes"

if not os.path.exists(outDir):
    os.makedirs(outDir)

javaFiles = []
for root, dirs, files in os.walk(srcDir):
    for f in files:
        if f.endswith(".java"):
            javaFiles.append(os.path.join(root, f))

print(f"Found {len(javaFiles)} Java files")

cmd = [javac, "-d", outDir, "-cp", cp] + javaFiles
print(f"Compiling...")
result = subprocess.run(cmd, capture_output=True, text=True, timeout=120)

if result.stdout:
    print("STDOUT:", result.stdout)
if result.stderr:
    print("STDERR:", result.stderr)
print(f"Exit code: {result.returncode}")

if result.returncode == 0:
    print("\n=== Creating JAR ===")
    jar = r"C:\Users\scroam\Downloads\MSL\Java\21\bin\jar.exe"
    jarFile = "ScroamDB-1.0.0.jar"
    
    if os.path.exists(jarFile):
        os.remove(jarFile)
    
    resources = "src\\main\\resources"
    if os.path.exists(resources):
        for f in os.listdir(resources):
            src = os.path.join(resources, f)
            dst = os.path.join(outDir, f)
            if os.path.isfile(src):
                import shutil
                shutil.copy(src, dst)
    
    cmd = [jar, "cf", jarFile, "-C", outDir, "."]
    result = subprocess.run(cmd, capture_output=True, text=True, timeout=30)
    print("JAR STDOUT:", result.stdout)
    print("JAR STDERR:", result.stderr)
    print("JAR Exit code:", result.returncode)
    
    if result.returncode == 0:
        print(f"\n=== Build successful! ===")
        print(f"JAR: {jarFile}")
        print(f"Size: {os.path.getsize(jarFile)} bytes")