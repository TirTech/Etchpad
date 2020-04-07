## How To
This page details the basic interactions with Etchpad. To change any settings, please see the Settings menu from the main screen dropdown.

### Terms
Canvas: The main drawing space shown when opening the app. This is where all drawing is done.

Pen/Cursor: The tool used to draw on the canvas. It is denoted by a circle drawn on the canvas. The pen has a size and color (both customizable).

Color Palette: The colors available to be drawn with. The current palette is shown in the top right corner of the canvas (referred to as the color palette widget).

Sensitivity: The speed the pen moves when the device is tilted in relation to *the angle the device is tilted at*. The pen moves faster as the device is tiled farther. A greater sensitivity means the pen will move faster overall.

Deadzone: The initial amount of tilt needed to start the pen moving. A bigger deadzone means a larger tilt needed to start drawing.

### Basics
#### Drawing
To draw on the canvas, simply tip the device in the direction that you would like the pen to travel. Drag the canvas to pan around the canvas. To change the color, tap the canvas. The currently selected color is indicated in the color palette widget with a black border. Colors will cycle when the end is reached. To adjust the pen size, use the slider at the top of the canvas. If the pen drifts, double tap to zero the tilt (what is considered "flat").

#### Undo
You can undo the current line drawn by shaking the device. When the line is undone, current color and pen size will be reset to the settings used to draw the line before the one undone, and your canvas will snap the view to this line.

#### Custom colors
To customize the color palette, tap on the color palette or choose Color Editor from the menu. To change a color in the editor, tap the color and adjust the color using the sliders. You may also add or remove colors from the palette (up to a min of 3 and a max of 10) using the Add and Remove buttons. When you are finished, tap Apply to set the colors or Cancel to revert any changes.

#### Save and Load
You may save and load the current canvas as JSON by choosing Save or Load from the menu. This will be saved in the application's file directory as the name given. Color Palettes are saved with the drawing. You may also export the canvas as a JPEG using the Export option from the menu.

#### Extras
##### Centering
If you get lost, you can center on your cursor using the Center on Cursor option from the menu. This happens automatically on undo.
##### Clearing
If you would like to clear the whole canvas, you may do so from the dropdown menu using the Clear action. This acts as a continuous undo.
