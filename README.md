# Metazelda

## Introduction

**I'm currently working on version 3.0 of Metazelda on this branch. The documentation is a little out of date right now. The branch 'version2' has documentation that matches the code.**

Implementation of an algorithm for procedurally-generating dungeons with
Zelda-like item-based puzzles.

![](http://bytten.net/random/metazelda2/2.png)

I plan to use this in my game,
[Lenna's Inception](http://bytten.net/devlog/tags.html#lennasinception-ref).

See also:
[my posts on metazelda on my devlog](http://bytten.net/devlog/tags.html#metazelda-ref)

## Building

Build using [eclipse](https://eclipse.org). Also requires my gameutil library,
which [you can find the source for on github](https://github.com/tcoxon/gameutil).

## Usage

To run after building:
    java -cp bin:viewer-bin net.bytten.metazelda.viewer.Main

It will randomly generate a graph and show it in the viewer.

Controls:

* F5 to generate a new dungeon.
* Pass -seed=X to generate a dungeon from a specific seed.
* Pass -color=colormaps/testcase.png to generate a free-form (non-grid) dungeon.
* Pass -space=spacemaps/skull.png to generate a grid dungeon in a specific shape.
* Pass -switches to generate dungeons with switch puzzles. This will turn off
  linearity optimization.
* Pass -no-goal to tell the generator not to make a goal room.

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
solvable _by construction_.

Dungeons are generated over several phases:

1. Create the entrance room
2. Create a tree of linked rooms (including locked doors)
3. Place the boss and goal rooms
4. Place the switch and switch-locks
5. Make the tree into a graph
6. Compute the intensity (difficulty) of rooms
7. Place keys within the dungeon
8. Optimizing linearity

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

Lock-and-key puzzles can be represented by graphs. The nodes of the graph
are the rooms, and the edges (aka links) are the doorways between the
rooms.

An edge is either conditional or unconditional. A conditional edge is
labelled with a Symbol, which means it's locked. The player must have that
symbol to be able to travel through that doorway. The player is always able
to travel through unconditional edges.

#### Key-Levels

This algorithm generates puzzles in such a way that to get the nth key, the
player must first get all keys 1 to n-1. This is a useful simplification as
it allows us to cheaply map the number of keys the player has collected to
the rooms the player is able to access.

The set of rooms accessible with n keys but inaccessible with n-1 keys is
known as key-level n. You can think of it as the nth level of the puzzle.

During generation, the algorithm keeps track of which rooms in the dungeon
are part of which key-levels so that symbols (keys) can be placed in rooms
to guarantee the puzzle's solvability: for every key-level n, the nth key
must appear in a key-level m, such that m < n.

#### Preconditions

The precondition of a room is the condition that must be true for the room
to be accessible to the player. This is the set of keys that the player
must have collected to be able to get into it (aka the key-level), and the
state that the switch must be in.

#### Intensity

Each room has an 'intensity,' a number between 0.0 and 1.0, which specifies
its relative difficulty within the dungeon. Clients of the library can use
this to decide which and how many enemies to place in the room. The
algorithm itself uses this number to put keys in the most difficult rooms.

#### Constraints

The metazelda package provides an API that allows you to apply several
different kinds of constraints on dungeon generation by implementing
net.bytten.metazelda.constraints.IDungeonConstraints.

The kinds of limitations you can impose on dungeons are:

1. Spacial limitations: forcing the dungeon into a particular shape. See
   the SpaceConstraints class.
2. Limit the number of rooms.
3. Limit the number of keys.
4. Specify whether the dungeon contains a switch puzzle.
5. Limit the coordinates that the initial room can be placed at.
6. Arbitrary post-generation checks: if the check fails, it will try
   generating another puzzle.

#### Retry

Some stages of the algorithm can fail, either due to the random nature of
the algorithm, or due to the externally-imposed constraints.

When this happens a RetryException is thrown. The generator catches this
and will attempt generation again (until after MAX\_RETRIES (20) attempts,
anyway).

### Creating the entrance room

The first step in generating the dungeon is placing the entrance room, the
starting point of the puzzle. This is recorded in the key-level room
mapping at key-level 0 (since no keys are required to access it).

### Creating the initial tree of rooms

The spanning tree of the eventual dungeon graph is generated by repeating
the following steps until there are no spaces left for rooms according to
the constraints placed on the generator. While this is going on, the
algorithm tracks the current key-level, which is initially 0.

1. Choose a random room that has already been placed with an edge bordering
an empty space. This is the parent room.
2. Randomly choose which adjacent empty space to extend into.
3. Create the new child room in this empty space, and link it to the parent
room.
4. The parent property of the child room references the parent room, and
the child list of the parent room is updated to include the child room.
5. At regular intervals, the current key-level is incremented, and the edge
to the parent room is made conditional based on the symbol for the new
current key-level.
6. The precondition for the child room is the current key-level.
7. The child room is added to the key-level room mapping for the current
key-level.

### Placing the boss and goal rooms

This part of the algorithm decides which rooms out of the ones already
placed will be the boss and the goal rooms.

1. It searches through all the rooms for empty dead-end rooms (ones with no
children), whose parent rooms are similarly empty and have only one child.
2. It filters out those rooms that are linked to their parents by
conditional edges.
3. It randomly chooses one of these rooms and makes it the goal room.
4. It makes the goal room's parent the boss room.
5. The goal room and the boss room are removed from their key-level, and
added to a new one above the highest one already placed.
6. The edge from the boss room to its parent is updated to be conditional
on the new key-level.

### Adding the switch puzzles

This works by:

1. Generating the path from the goal room to the start room by following
the parent relation on each room.
2. Picking a random room in that path to act as the base of the switch
puzzle. (Using the 'solution' path ensures that the switch puzzle has
significance to solving the puzzle.)
3. Randomly locking the base room's links to its children with conditions
that require the switch to be in a particular state. If an immediate child
is already locked, then all of _its_ children are locked with the same
switch-state condition, and so on.
4. Placing the switch object in any room that is not a descendant room of
the base room, and is at the same or lower key-level.

### Making the spanning tree into a graph

Until this phase, the dungeon graph is a spanning tree. The graphify phase
randomly links up neighboring rooms so that the graph is not a simple tree.

Some notes on how it does this without trivializing the puzzle:

1. The boss and goal rooms are not touched.
2. Rooms can be linked by an unconditional edge if their preconditions are
the same.
3. Rooms whose preconditions are not the same can be linked by a
conditional edge if their preconditions differ by a single symbol, and the
condition on the edge is that symbol.

### Computing the intensity of rooms

Intensity is initially applied with numbers outside of the usual 0.0-1.0
range by recursively visiting every node of the spanning tree of the
dungeon.

Every room's child rooms in the same key-level will have a higher intensity
than it, and rooms in the next key-level start at a bit below the highest
intensity of the key-level below it.

Afterwards, all intensities in the dungeon are scaled to between 0.0 and
1.0.

This gives the puzzle a jagged tension curve as found in
[Calvin Ashmore's thesis on Key and Lock Puzzles in Procedural Gameplay](https://docs.google.com/viewer?a=v&q=cache:07ET5YiF2x8J:citeseerx.ist.psu.edu/viewdoc/download%3Fdoi%3D10.1.1.84.9619%26rep%3Drep1%26type%3Dpdf+&hl=en&gl=uk&pid=bl&srcid=ADGEEShA38IAX2EhYksmBbw5YKzbgj1u9zd-sacNOZQGPNy77O2pApyEpruteKRhgm0kRlVneVF77eTEpPUPDt_JiBWWdwYn3ksJFE1S4OcrAD-I9AN5WOF7cClMr8HsEB_eSD3MWVRs&sig=AHIEtbQLn3edtPGvxpf0BmNSdiM_-5DKRQ).

### Placing keys within the dungeon

Finally, placing the keys in the dungeon in such a way that the puzzle is
solvable is the simplest part: the key for each key-level n > 0 is placed
in the highest-intensity room in key-level n-1.

### Optimizing linearity

This phase is specific to the LinearDungeonGenerator class and does not affect
the base DungeonGenerator class.

According to [this insightful article on Gamasutra](http://www.gamasutra.com/view/feature/6582/learning_from_the_masters_level_.php),
true nonlinearity (i.e. backtracking) in a level is undesirable. It's simply
boring for players to repeatedly go back and forth between a few rooms. On the
other hand, an appearance of nonlinearity _is_ desirable. The difference is that
the backtracking should be optional: the shortest path to the goal should not
require the player to repeatedly traverse the same path.

LinearDungeonGenerator generates several dungeons with DungeonGenerator,
and uses its measure of nonlinearity to pick the most linear). This does not
stop dungeons _appearing_ nonlinear, because there will still be optional
branches in the dungeons that are generated.

Nonlinearity in Metazelda is defined as the number of times a player must walk
through each room _after its first encounter_, on the shortest path through the
dungeon.

LinearDungeonGenerator uses A* to find the shortest path through the dungeon:
it is the shortest path from the entrance to the first key, from each key to the
next key and from the last key to the goal. Locked doors along the way are
respected so that the shortest path does not pass through doors that are locked
with a key that hasn't shown up in the path so far.

It then uses this shortest path to count the rooms that are traversed multiple
times.

Dungeons with switches in them tend to have a lot of backtracking, and dungeons
without lots of backtracking tend not to use the switches for anything
significant. This is why linearity optimization is disabled when the -switches
option is passed to the viewer application.
