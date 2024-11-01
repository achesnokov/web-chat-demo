# AWS Credentials Setup Guide

This guide provides detailed instructions on how to obtain your `aws_access_key_id` and `aws_secret_access_key` from the AWS Management Console and save them to the `.aws/credentials` file for use in your deployment.

## Step-by-Step Instructions

### Step 1: Create or Access Your AWS Account

If you don't have an AWS account yet, follow these steps to create one:
1. Visit [AWS Sign Up](https://aws.amazon.com/).
2. Click **Create an AWS Account** and follow the instructions.
3. Enter your email address, set a password, and complete the account setup process.

Once you have an AWS account, proceed to log in to the AWS Management Console.

### Step 2: Navigate to the IAM Console

The next step is to access AWS IAM (Identity and Access Management) to create user credentials:
1. From the [AWS Management Console](https://aws.amazon.com/console/), type **IAM** in the search bar and select **IAM**.
2. On the IAM dashboard, click **Users** from the left-hand menu.
3. Click the **Add Users** button.
4. Choose a username (e.g., `web-chat-deploy`) and select **Programmatic access** to generate access keys.

### Step 3: Set User Permissions

1. On the next page, choose how to set permissions for this user:
    - You can either **Attach policies directly** or **Add user to group**.
    - For simplicity, you can attach the **AdministratorAccess** policy directly to the user, which will grant full access to AWS services.

   > **Note**: Administrator access should only be used for demonstration purposes. For production environments, consider using a policy with more restrictive permissions.

2. Click **Next: Tags** (optional) and add any tags you want to use for organization.
3. Click **Next: Review**, verify the details, and click **Create User**.

### Step 4: Download Access Keys

After creating the user, you will be shown the `Access Key ID` and `Secret Access Key`.
1. Click **Download .csv** to save the credentials to your computer, or manually copy both the `Access Key ID` and `Secret Access Key`.
2. **Important**: Keep these credentials secure. Do not share them or expose them in public repositories.

### Step 5: Configure AWS CLI

To make use of these credentials in your application, you need to save them in the `.aws/credentials` file on your local machine:
1. Open a terminal.
2. Run the following command to configure your AWS credentials:
   ```sh
   aws configure
   ```
3. When prompted, enter your `Access Key ID` and `Secret Access Key`, along with the default region name (e.g., `eu-central-1`) and output format (`json` is recommended).

Alternatively, you can manually add these credentials to the `.aws/credentials` file. Locate (or create) the file at `~/.aws/credentials` and add the following lines:
```ini
[default]
aws_access_key_id = YOUR_ACCESS_KEY_ID
aws_secret_access_key = YOUR_SECRET_ACCESS_KEY
```
Replace `YOUR_ACCESS_KEY_ID` and `YOUR_SECRET_ACCESS_KEY` with the values from Step 4.

### Step 6: Verify the Setup

To verify that your credentials are set up correctly, you can run a simple AWS CLI command to list available S3 buckets:
```sh
aws s3 ls
```
If the credentials are set correctly, you should see a list of your S3 buckets (or an empty list if none exist).

## Security Considerations
- **Never hard-code credentials** in your application code.
- **Do not share credentials** publicly or commit them to version control.
- Consider using **AWS IAM Roles** for secure access in production environments instead of static credentials.

## Troubleshooting
If you encounter issues during this process:
- Ensure that you have the correct permissions assigned to the IAM user.
- Make sure that the `.aws/credentials` file is saved with the correct syntax and location.
- Double-check that your access keys have not expired or been deactivated.

For more help, visit the [AWS IAM User Guide](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_users.html).

