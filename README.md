# Elite Trainer Boxing

Elite Trainer Boxing is an Android boxing training app built for Solana Mobile / Seeker.

The app combines guided boxing training with Solana wallet connectivity and on-chain payments, allowing users to unlock the VS Pack Pro directly from a compatible Solana wallet.

## Solana Mobile Hackathon Build

This repository contains the latest hackathon version of Elite Trainer Boxing.

### Solana integration

- Solana Mobile Wallet Adapter (MWA)
- Solana Mainnet wallet connection
- Wallet connection persists between sessions
- Proper wallet disconnect support
- Solana Pay-style payment flow
- On-chain payment verification before Pro access is activated
- Pro access is linked to the connected Solana wallet

## SKR Integration

Elite Trainer Boxing supports SKR as a payment option for VS Pack Pro.

**150 SKR = 30 days of VS Pack Pro access**

SKR mint:

`SKRbvo6Gf7GondiT3BbTfuRDPqLWei4j2Qy2NPGZhW3`

Before wallet approval, the app clearly displays the exact SKR amount.

After payment, the transaction is checked on-chain before Pro access is activated.

## USDC Integration

Users can alternatively activate the same Pro access using USDC.

**2.50 USDC = 30 days of VS Pack Pro access**

Both SKR and USDC provide the same 30-day entitlement.

Access does not automatically renew.

## VS Pack Pro

VS Pack Pro is designed for style-vs-style boxing training.

It provides training scenarios that help users practise against different boxing approaches, matchups and tactical situations.

The app provides training information including:

- Game plans
- Things to watch for
- Training focus
- Orthodox and southpaw situations
- Different boxing styles and tactical matchups

## Main App Features

- Guided boxing combinations
- Voice-assisted training
- Timed boxing rounds
- Boxing style selection
- Training speed controls
- Orthodox and southpaw training
- VS Pack Pro
- Solana wallet integration
- SKR payments
- USDC payments

## Technology

- Android
- Kotlin
- Android WebView
- HTML / JavaScript training interface
- Solana Mobile Wallet Adapter
- Solana Mainnet
- SPL token payments
- Server-side payment verification

The Android application communicates with the Elite Trainer Boxing verification service to confirm qualifying blockchain payments before Pro access is granted.

## Building the App

1. Clone this repository.
2. Open the project in Android Studio.
3. Allow Gradle dependencies to sync.
4. Build and run the Android application on a compatible Android / Solana Mobile device.
5. Connect a compatible Solana wallet from inside the app.

Release signing files are intentionally not included in this public repository.

## Security

Private signing keys, keystores, local Android configuration files, environment files and other secrets are excluded from this repository.

Pro access is not granted simply by pressing the payment button. The qualifying blockchain payment must be verified before access is activated.

## Project

**Elite Trainer Boxing**

Built by **Elite Trainer Apps**

Created for Android and Solana Mobile / Seeker.
