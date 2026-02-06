<p align="center">
    <a href="https://t.me/curexcrate_bot">
        <img src="https://helltar.com/projects/curexcrate-bot/img/t-me-qr-code.png" alt="qr_code" width="35%">
    </a>
</p>

### Docker

```bash
docker run -d \
  --name curexcrate-bot \
  --restart unless-stopped \
  -e CREATOR_ID=12345 \
  -e BOT_TOKEN=123:xxx \
  -e BOT_USERNAME=my_bot \
  ghcr.io/helltar/curexcrate-bot:latest
```

### Usage

- `/cur` - currency conversion (e.g. `/cur 1 usd to uah`)
