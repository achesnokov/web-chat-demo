### Instructions for Creating an EC2 KeyPair

To deploy an EC2 instance using AWS SAM and allow SSH access, you need an EC2 KeyPair. Below are two methods to create a KeyPair: using **AWS Management Console** and **AWS CLI**.

#### Option 1: Creating an EC2 KeyPair Using the AWS Management Console

1. **Open the Amazon EC2 Console**:
    - Go to the EC2 Management Console: https://console.aws.amazon.com/ec2/

2. **Navigate to Key Pairs**:
    - In the left-hand menu, select **"Key Pairs"** under **"Network & Security"**.

3. **Create a New Key Pair**:
    - Click on the **"Create Key Pair"** button.
    - Enter a name for your key pair, such as `web-chat-demo-keypair`.
    - Choose the **Key Pair Type** as **RSA**.
    - Set the **Private Key File Format** as **.pem** (recommended for Linux and Mac) or **.ppk** (for PuTTY on Windows).

4. **Download the Key Pair**:
    - After clicking **"Create"**, your private key (`.pem` file) will be automatically downloaded. **Make sure to store it securely**, as AWS does not provide an option to download it again.
    - You will need this key to SSH into your EC2 instance.

#### Option 2: Creating an EC2 KeyPair Using AWS CLI

1. **Install AWS CLI** (if not already installed):
    - Follow the instructions here: https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html

2. **Configure AWS CLI**:
    - Run the command: `aws configure`
    - Enter your **AWS Access Key ID**, **Secret Access Key**, **Default region name**, and **Default output format**.

3. **Create a Key Pair**:
    - Use the following command to create a key pair:

      ```sh
      aws ec2 create-key-pair --key-name web-chat-demo-keypair --query 'KeyMaterial' --output text > web-chat-demo-keypair.pem
      ```
    - This command will create a key pair named `web-chat-demo-keypair` and save the private key as `web-chat-demo-keypair.pem` in the current directory.

4. **Set Permissions on the Key Pair**:
    - Ensure the key file has the correct permissions by running:

      ```sh
      chmod 400 web-chat-demo-keypair.pem
      ```
    - This is required to securely connect to your instance via SSH.

#### Important Notes
- **Security**: Store your private key (`.pem` file) securely. It provides access to your EC2 instances and should not be shared.
- **SSH Access**: To connect to your EC2 instance, use the command:

  ```sh
  ssh -i "web-chat-demo-keypair.pem" ec2-user@<Public-IP-of-EC2-Instance>
  ```
  Replace `<Public-IP-of-EC2-Instance>` with the actual public IP address of your instance, which can be found in the AWS Management Console or via the Outputs of your CloudFormation/SAM stack.

