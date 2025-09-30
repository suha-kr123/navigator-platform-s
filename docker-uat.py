import subprocess

def run_command(command):
    """Run a shell command and print its output live."""
    process = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    for line in process.stdout:
        print(line, end="")
    for line in process.stderr:
        print(line, end="")
    process.wait()
    return process.returncode

def main():
    region = "ap-south-1"
    account_id = "600627336794"
    repo_name = "navigator-platform"

    # Ask for the tag
    tag = input("Enter Docker tag (default: from git rev-parse --short HEAD): ").strip()
    if not tag:
        try:
            tag = subprocess.check_output("git rev-parse --short HEAD", shell=True, text=True).strip()
            print(f"ℹ️  Using git commit hash as tag: {tag}")
        except subprocess.CalledProcessError:
            print("❌ Could not get tag from git. Please enter manually.")
            return

    image_name = f"{repo_name}:{tag}"
    ecr_repo = f"{account_id}.dkr.ecr.{region}.amazonaws.com/{repo_name}:{tag}"

    # Step 0: Gradle build
    print("\n🛠️ Running Gradle build...")
    gradle_cmds = ["./gradlew clean", "./gradlew :main:build"]
    for cmd in gradle_cmds:
        if run_command(cmd) != 0:
            print(f"❌ Gradle step failed: {cmd}")
            return

    # Step 1: AWS ECR Login
    print("\n🔑 Logging into AWS ECR...")
    login_cmd = f"aws ecr get-login-password --region {region} | docker login --username AWS --password-stdin {account_id}.dkr.ecr.{region}.amazonaws.com"
    if run_command(login_cmd) != 0:
        print("❌ ECR login failed")
        return

    # Step 2: Docker Build
    print("\n🐳 Building Docker image...")
    build_cmd = f"docker build --platform linux/amd64 -t {image_name} ."
    if run_command(build_cmd) != 0:
        print("❌ Docker build failed")
        return

    # Step 3: Docker Tag
    print("\n🏷️ Tagging Docker image...")
    tag_cmd = f"docker tag {image_name} {ecr_repo}"
    if run_command(tag_cmd) != 0:
        print("❌ Docker tagging failed")
        return

    # Step 4: Docker Push
    print("\n📤 Pushing Docker image to ECR...")
    push_cmd = f"docker push {ecr_repo}"
    if run_command(push_cmd) != 0:
        print("❌ Docker push failed")
        return

    print(f"\n✅ Successfully built and pushed {ecr_repo}")

if __name__ == "__main__":
    main()