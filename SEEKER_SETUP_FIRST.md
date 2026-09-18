# Elite Trainer Boxing — Solana Seeker

This is the separate Solana Mobile version. It does not contain Google Play Billing and does not alter the Google Play project.

## App

- App name: `Elite Trainer Boxing · Solana`
- Package: `com.elitetrainer.boxing.solana`
- Main HTML: `app/src/main/assets/elite_boxing.html`
- Main Android file: `app/src/main/java/com/elitetrainer/boxing/solana/MainActivity.kt`
- Network: Solana Mainnet

Open this whole folder in Android Studio, allow Gradle to sync, then build or run it like the other Seeker apps.

## Wallet and Boxing Pro

- Connects through Mobile Wallet Adapter, so Seed Vault and other compatible wallets can appear.
- The connected wallet and MWA authorisation are remembered after closing the app.
- The connected button becomes `DISCONNECT WALLET`, fully clears the MWA authorisation and allows another wallet to be selected.
- Boxing Trainer is included.
- Boxing VS Pack costs `2.50 USDC` for `30 days`.
- The backend verifies the exact USDC payment came from the same connected wallet before unlocking Pro.
- No local flag can grant paid access.

## Live payment backend

The Boxing payment backend is live at `https://elite-trainer-boxing.vercel.app/api/pro` and was endpoint-tested on 15 September 2026. The matching source is included in `solana-backend` for future maintenance.

No backend deployment is required before building this version. If it ever needs redeploying:

1. Create or update the separate Vercel project named `elite-trainer-boxing` from that folder.
2. The receiving address is already set to `GR4uaBSvuKN56CKk9xaMEB2U9PeLQofDVhESp5JdbBMS`, the same public receiving wallet used by Elite Trainer Striking.
3. An optional private `SOLANA_RPC_URL` can be added for higher traffic. Without it, the server uses Solana's public Mainnet RPC.
4. Never place a seed phrase or private key in Vercel or in this project. The receiving wallet must have a Mainnet USDC associated token account before accepting payments.
5. Deploy it at `https://elite-trainer-boxing.vercel.app`.
6. If Vercel gives it a different address, change `PRO_API_BASE` near the bottom of `elite_boxing.html`.

Do not point this app at the Striking subscription backend. Boxing uses its own product identity and exact Boxing memo.

The backend creates the unsigned 2.50-USDC transaction. The app sends it back through Mobile Wallet Adapter, checks that MWA returned the same connected account, and asks that wallet to sign and submit it. The backend verifies the exact payer, recipient, amount, signature and Boxing memo directly on Solana, then calculates 30 days of access from the confirmed blockchain timestamp. It cannot be unlocked by changing a local flag.

## Terms and privacy before submission

The app contains its full Boxing/Solana Terms and Privacy Notice and requires acceptance. The files `WEBSITE_PRIVACY_POLICY_UPDATE.txt` and `WEBSITE_TERMS_OF_USE.txt` contain matching website copy. Paste those into the corresponding public Squarespace pages before store submission, because the existing general privacy page does not yet describe Solana wallet/payment records.
