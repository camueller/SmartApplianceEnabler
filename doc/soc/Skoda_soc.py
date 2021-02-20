#!/use/bin/env python3
# coding: utf8

import asyncio
import logging

from aiohttp import ClientSession
from skodaconnect import Connection

logging.basicConfig(level=logging.ERROR)

USERNAME = 'XXX'
PASSWORD = 'YYY'
PRINTRESPONSE = False


async def main():
    """Main method."""
    async with ClientSession(headers={'Connection': 'keep-alive'}) as session:
        print(f"Initiating new session to Skoda Connect with {USERNAME} as username")
        connection = Connection(session, USERNAME, PASSWORD, PRINTRESPONSE)
        if await connection._login():
            vehicle = connection.vehicles[0]
            print("state_of_charge %s" % str(vehicle.battery_level))


if __name__ == "__main__":
    asyncio.run(main())
