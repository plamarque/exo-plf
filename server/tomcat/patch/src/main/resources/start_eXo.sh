#!/bin/sh
#
# Copyright (C) 2009 eXo Platform SAS.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.
#

# Computes the absolute path of eXo
cd `dirname "$0"`

echo Starting eXo ...

cd ./bin

if [ "$1" = "" ] ; then 
	EXO_PROFILES="-Dexo.profiles=default"
else
	EXO_PROFILES="-Dexo.profiles=$1"
fi


echo eXo is launched with the $EXO_PROFILES option as profile

if [ -r ./gatein.sh ]; then
	exec ./gatein.sh run
else
	echo gatein.sh is missing.
fi