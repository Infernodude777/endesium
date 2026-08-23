# AGENTS.md

## Tools
- Don't use .\gradlew tool- it bugs out for agents; instead just avoid it and go around it.

## Git
- After every run/session thats not just updating docs or mds or similar or if it is setting up Jimbibo, commit and push to GitHub with a nice, clean, concise commit message.
- Stage only relevant files; never commit secrets.
- Never mention Jimbibo in commits

## Jimbibo
- Always launch plans with ESC safety disabled (`no_safety: true`) so pressing ESC never aborts a run; every planned file must complete regardless of keypresses.