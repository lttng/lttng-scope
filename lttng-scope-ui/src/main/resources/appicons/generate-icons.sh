#!/bin/bash

# This script will generate all variants of the icon, based on:
# png/scope_icon_256x256.png
#
# Requires:
#  - imagemagick ("convert" command)
#  - icnsutils   ("png2icns" command)
#  - icoutils    ("icotool" command)

# generate the different sizes of PNG
for i in 16 32 48 64 128
do
  convert png/scope_icon_256x256.png -resize "$i"x"$i" png/scope_icon_"$i"x"$i".png
done

# generate the different BMPs
#
# Eclipse asks for "32-bit BMPs", but that doesn't seem to exist... unless it
# means "BMP with an alpha channel". The commands below produce '-x8' BMPs with
# no alpha channel, and '-x32' ones with a 1-bit apha channel.
# Hopefully that works.
mkdir bmp
for i in 16 32 48
do
  convert png/scope_icon_256x256.png -resize "$i"x"$i" -colors 256 bmp/scope_icon_"$i"x"$i"x8.bmp
  convert png/scope_icon_256x256.png -resize "$i"x"$i" -depth 32 bmp/scope_icon_"$i"x"$i"x32.bmp
done
convert png/scope_icon_256x256.png -depth 32 bmp/scope_icon_256x256x32.bmp
convert bmp/*.bmp ico/scope_icon.ico
rm bmp/*.bmp
rmdir bmp

# generate the XPM
convert png/scope_icon_256x256.png xpm/scope_icon_256x256.xpm

# generate the ICNS
png2icns icns/scope_icon.icns png/scope_icon_256x256.png png/scope_icon_128x128.png png/scope_icon_32x32.png png/scope_icon_16x16.png
