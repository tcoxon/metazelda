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
net.bytten.metazelda.generators.DungeonGenerator class.


