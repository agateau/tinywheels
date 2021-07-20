# Building Pixel Wheels

## Dependencies

Building Pixel Wheels requires the following components:

- A JDK
- libgdx: <https://libgdx.badlogicgames.com>
- ImageMagick: <http://imagemagick.org>
- GNU Make: <http://www.gnu.org/software/make/>
- Some Python packages:
    - PAFX: <https://github.com/agateau/pafx>
    - Pillow: <https:python-pillow.github.io/>
    - pypng: <https://github.com/drj11/pypng>
    - jinja2: <https://palletsprojects.com/p/jinja/>
    - polib: <https://github.com/izimobil/polib/>

### Python packages

You can install the required Python packages with:

    pip3 install -r requirements.txt

## Assets

Some assets must be generated from work files with:

    make assets

## Pack images

Once assets have been generated, you can pack them into atlases with:

    make packer

## Compile translations

Translations files (.po) must be turned into Java files with:

    make po-compile

## Build the game

Run:

    make

You can also build and run it with:

    make run
