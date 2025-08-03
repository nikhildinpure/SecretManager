The Master password has to be remembered if lost no recovery.
Planned use cases.

        1. File encryption decryption
           a. encrypt file or all files in path
                i. validate password strength
                ii. encrypt files
           b. decrypt file or all .enc files in path
                i. decrypt files
           c. Expose APIs and Main for same
           d. Password reset (if possible/optional)
                i. validate older password
                ii. Decrypt all files with older password
                iii. encrypt all files with new password.

       2. Password vault
          a. master password validation
          a. store encrypted passwords in json
          b. Show password of specific account
          c. remove password of specific account
    
          3. MFA integration for reset password
    
          4. Local UI and Database
    
          5. Security Review and remediation