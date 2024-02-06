import argparse
from pathlib import Path
from typing import NamedTuple

import pandas as pd
import xmltodict
from hypy_utils import write


def read_list(search_path: str, pattern: str, parse_fn) -> list:
    search_path = path / search_path
    data = [parse_fn(xmltodict.parse(t.read_text('utf-8')))
            for t in list(search_path.glob(pattern))]
    Path('csv').mkdir(exist_ok=True, parents=True)
    pd.DataFrame(data).to_csv(f'csv/{search_path.name}.csv', index=False)
    return data


class Ticket(NamedTuple):
    id: int  # TicketData.name.id
    name: str  # TicketData.name.str
    credits: int  # TicketData.creditNum
    kind: str  # TicketData.ticketKind.str
    max: int  # TicketData.maxCount
    detail: str  # TicketData.detail
    eventId: int  # TicketData.ticketEvent.id
    eventName: str  # TicketData.ticketEvent.str


def parse_ticket(d: dict) -> Ticket:
    return Ticket(
        id=int(d['TicketData']['name']['id']),
        name=d['TicketData']['name']['str'],
        credits=int(d['TicketData']['creditNum']),
        kind=d['TicketData']['ticketKind']['str'],
        max=int(d['TicketData']['maxCount']),
        detail=d['TicketData']['detail'],
        eventId=int(d['TicketData']['ticketEvent']['id']),
        eventName=d['TicketData']['ticketEvent']['str']
    )


class Event(NamedTuple):
    id: int  # EventData.name.id
    type: int  # EventData.infoType
    detail: str  # EventData.name.str
    alwaysOpen: bool  # EventData.alwaysOpen


def parse_event(d: dict) -> Event:
    return Event(
        id=int(d['EventData']['name']['id']),
        type=int(d['EventData']['infoType']),
        detail=d['EventData']['name']['str'],
        alwaysOpen=bool(d['EventData']['alwaysOpen'])
    )


if __name__ == '__main__':
    agupa = argparse.ArgumentParser(description='Convert maimai data to csv')
    agupa.add_argument('path', type=Path, help='Path to A000 data folder')
    args = agupa.parse_args()
    path = Path(args.path)

    tickets = read_list('ticket', '*/Ticket.xml', parse_ticket)

    events = read_list('event', '*/Event.xml', parse_event)

    # Write incremental sql
    ids = [int(v.split(",")[0]) for v in (Path(__file__).parent / 'maimai2_game_event.csv').read_text().splitlines()]
    new_events = [e for e in events if e.id not in ids]
    sql = "INSERT INTO `maimai2_game_event` (`id`, `end_date`, `start_date`, `type`, `enable`) VALUES \n" + \
        ",\n".join([f"({e.id}, '2029-01-01 00:00:00.000000', '2019-01-01 00:00:00.000000', {e.type}, '1')" for e in new_events])
    sql += ";\n"
    write('sql/maimai2_game_event.sql', sql)
