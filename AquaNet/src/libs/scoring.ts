export type GameName = 'mai2' | 'chu3' | 'ongeki'

const multTable = {
  'mai2': [
    [ 100.5, 22.4, 'SSSp' ],
    [ 100.0, 21.6, 'SSS' ],
    [ 99.5, 21.1, 'SSp' ],
    [ 99, 20.8, 'SS' ],
    [ 98, 20.3, 'Sp' ],
    [ 97, 20, 'S' ],
    [ 94, 16.8, 'AAA' ],
    [ 90, 15.2, 'AA' ],
    [ 80, 13.6, 'A' ]
  ],

  // TODO: Fill in multipliers for Chunithm and Ongeki
  'chu3': [
    [ 100.75, 0, 'SSS' ],
    [ 100.0, 0, 'SS' ],
    [ 97.5, 0, 'S' ],
    [ 95.0, 0, 'AAA' ],
    [ 92.5, 0, 'AA' ],
    [ 90.0, 0, 'A' ],
    [ 80.0, 0, 'BBB' ],
    [ 70.0, 0, 'BB' ],
    [ 60.0, 0, 'B' ],
    [ 50.0, 0, 'C' ],
    [ 0.0, 0, 'D' ]
  ],

  'ongeki': [
    [ 100.75, 0, 'SSS+' ],
    [ 100.0, 0, 'SSS' ],
    [ 99.0, 0, 'SS' ],
    [ 97.0, 0, 'S' ],
    [ 94.0, 0, 'AAA' ],
    [ 90.0, 0, 'AA' ],
    [ 85.0, 0, 'A' ],
    [ 80.0, 0, 'BBB' ],
    [ 75.0, 0, 'BB' ],
    [ 70.0, 0, 'B' ],
    [ 50.0, 0, 'C' ],
    [ 0.0, 0, 'D' ]
  ]
}


export function getMult(achievement: number, game: GameName) {
  achievement /= 10000
  const mt = multTable[game]
  for (let i = 0; i < mt.length; i++) {
    if (achievement >= (mt[i][0] as number)) return mt[i]
  }
  return [ 0, 0, 0 ]
}