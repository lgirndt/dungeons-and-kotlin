// MongoDB initialization script to create dev user for dungeon database
db = db.getSiblingDB('dungeon');

db.createUser({
  user: 'dev',
  pwd: 'dev',
  roles: [
    {
      role: 'readWrite',
      db: 'dungeon'
    }
  ]
});

print('✅ Created dev user with readWrite access to dungeon database');

db.player.createIndex( { "name": 1 }, { unique: true } )

