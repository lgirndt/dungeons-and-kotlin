# Reusable UI Menu Component

This document describes the requirements and design considerations for a reusable UI menu component that can 
be used in the cli kotter application.

## Data Structure

The menu can show a hierarchical menu item structure. A menu item can
- display a label
- reference a domain object. Therefore the Menu will be generic over a type T
- contains a list of children

## UI Behavior

It will be rendered always one level of the tree, which is the children of the currently selected menu item.
A newly started component starts with the root element as the current element.

It must be possible to select a single menu item. There can be keys registered for selecting the previous / next
item. Default are up and down keys. The menu is displayed in a vertical list. the currently selected item
id highlighted. The for and backfground colors can be configured.

The selected item can be activated. The key for the activation can be configured, default is "Return". If the
activated item has children, the current level is changes to the activated item, and its children are displayed.
If the activated item has no children, a callback is called with the domain object of the activated item.

## Example

Given the local data model

What are you going to do?
    Go To
        The northern door
        The western hallway
    Open
        The trunk
    Invetigate
        The strange noise
        The dark passage

The root menu item is "What are you going to do?", which has three children: "Go To", "Open", and "Investigate".

The initial render will look like this
```
What are you going to do?

>Go to<
 Open
 Investigate
```

If the user presses the down key twice, the render will look like this
```
What are you going to do? 

 Open
 Investigate
>Investigate<
```

If the user presses the Return key, the render will look like this
```
Investigate

>The strange noise<
 The dark passage
```
