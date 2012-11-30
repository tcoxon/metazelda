# Metazelda

## Introduction

Implementation of an algorithm for procedurally-generating dungeons with
Zelda-like item-based puzzles.

![](http://bytten.net/random/metazelda2/2.png)

I plan to use this in my game,
[Lenna's Inception](http://bytten.net/devlog/tags.html#lennasinception-ref).

See also:
[my posts on metazelda on my devlog](http://bytten.net/devlog/tags.html#metazelda-ref)

## Usage

To run after building:
    java -cp bin:viewer-bin net.bytten.metazelda.viewer.Main

It will randomly generate a graph and show it in the viewer.

Controls:

* F5 to generate a new dungeon.
* Pass -seed=X to generate a dungeon from a specific seed.
* Pass -space=spacemaps/skull.png to generate a dungeon in a specific shape.

Reading the graph:

* Circles are rooms, black edges joining two circles are links (doorways
  between two rooms).
* The player starts at the room containing 'Start' and the aim is to get to
  the room containing 'Goal'.
* Rooms can contain symbols - represented by letters. The player collects
  the symbols of the rooms they've passed through.
* Edges with letters on them require the player to have collected the
  symbol with that letter to unlock that door.
* The colors are a heatmap of the relative difficulties of the rooms in the
  dungeon. Red - difficult; blue - easy.
* The switch object ('SW') can be put into one of two states: ON or OFF.
  Edges in the dungeon graph that have one of the words ON or OFF on them
  require the switch to be in that state for the door to unlock.

## Screenshots

![](http://bytten.net/random/metazelda2/1.png)
![](http://bytten.net/random/metazelda2/3.png)
![](http://bytten.net/random/metazelda2/4.png)
![](http://bytten.net/random/metazelda2/5.png)

## Algorithm

The following algorithm for generating dungeons is implemented in the
net.bytten.metazelda.generators.DungeonGenerator class's generate() method.

This algorithm generates lock-and-key puzzles that are guaranteed to be
solvable.

Dungeons are generated over several phases:

1. Create the entrance room
2. Create a tree of linked rooms (including locked doors)
3. Place the boss and goal rooms
4. Place the switch and switch-locks
5. Make the tree into a graph
6. Compute the intensity (difficulty) of rooms
7. Place keys within the dungeon

### Definitions

First of all, some definitions are needed so that you will understand what
I mean by 'dungeon,' 'edge,' 'symbol,' and so on.

I use the word 'dungeon' interchangeably with 'puzzle,' but there's no
reason this algorithm couldn't be used for non-dungeon lock-and-key
puzzles.

#### Keys, Symbols

Let's forget for now that the keys and locks are items and objects in the
game and consider an abstraction: a key is a boolean variable --
you either have it or you don't -- and a lock is a test of this variable.
We'll give each of these boolean variables its own letter, i.e. A, B, C.

Each boolean variable is initially false (the player starts with no keys),
but goes true when the player collects the key. For the sake of simplicity,
we will say these variables never go false again: our puzzles effectively
have colored keys (such as in Doom) that can be reused rather than the
small keys in Zelda that can only be used once.

In Metazelda, we keep track of 'Symbols' to figure out which boolean
variables have to be, or might be true.

Rooms containing Symbols are said to give the player that Symbol (they set
the boolean variable for that Symbol to true). Locks are also labelled with
Symbols, and can only be unlocked if the player has that Symbol (if the
boolean variable is true).

#### Room Graphs

Rooms are nodes in the graph.

Rooms are 'linked' by doorways, which are represented by Edges in the graph.
An Edge is optionally labelled by a Symbol, which means it's locked.

### Creating the entrance room

### Creating the initial tree of rooms

### Placing the boss and goal rooms

### Making the room tree into a graph

### Computing the intensity of rooms

### Placing keys within the dungeon

