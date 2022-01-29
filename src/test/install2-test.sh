#!/bin/bash
#
# Copyright (C) 2022 Axel Müller <axel.mueller@avanux.de>
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#
MOUNT_POINT=/mnt
WORK_DIR=/tmp
INSTALL_DIR=/usr/local/bin
IMAGE_FILE=2021-05-07-raspios-buster-armhf-lite.img
SCRIPT2=install2.sh
PARENT_SCRIPT=/etc/rc.local
PARTITION_OFFSET=272629760 # =532480 * 512

if [ "$EUID" -ne 0 ]
  then echo "Must be run as root"
  exit
fi

echo "Get image ..."
rm -f $WORK_DIR/$IMAGE_FILE
cp /data/IdeaProjects/RaspiOSImageAutorunUSBShellScript/$IMAGE_FILE $WORK_DIR

echo "Increase image size"
fdisk -l $WORK_DIR/$IMAGE_FILE
dd if=/dev/zero bs=1M count=2048 >> $WORK_DIR/$IMAGE_FILE
cat <<EOF | parted ---pretend-input-tty $WORK_DIR/$IMAGE_FILE
resizepart 2 -0
yes
EOF
fdisk -l $WORK_DIR/$IMAGE_FILE

echo "Increase file system size of device with offset $PARTITION_OFFSET"
LOOP_DEVICE=`losetup --offset=$PARTITION_OFFSET --find --show $WORK_DIR/$IMAGE_FILE`
e2fsck -fy $LOOP_DEVICE
resize2fs $LOOP_DEVICE
e2fsck -fy $LOOP_DEVICE
losetup --detach $LOOP_DEVICE

echo "Mount image with offset 272629760 (=532480 * 512)"
mount -v -o offset=$PARTITION_OFFSET -t ext4 $WORK_DIR/$IMAGE_FILE $MOUNT_POINT
df

echo "Install installation script"
wget https://github.com/camueller/SmartApplianceEnabler/raw/master/install/$SCRIPT2 -P $MOUNT_POINT$INSTALL_DIR
chmod +x $MOUNT_POINT$INSTALL_DIR/$SCRIPT2
echo "#!/bin/sh -e" > $MOUNT_POINT/$PARENT_SCRIPT
echo "nohup $INSTALL_DIR/$SCRIPT2 &" >> $MOUNT_POINT/$PARENT_SCRIPT
echo "exit 0" >> $MOUNT_POINT/$PARENT_SCRIPT
chmod +x $MOUNT_POINT/$PARENT_SCRIPT

echo "Install installation configuration file"
wget https://github.com/camueller/SmartApplianceEnabler/raw/master/install/install.config -P $MOUNT_POINT/usr/local/etc

echo "Unmount $MOUNT_POINT"
umount $MOUNT_POINT

echo "Boot virtual Raspberry Pi"
qemu-system-arm -M versatilepb -cpu arm1176 -m 256 -drive "file=$WORK_DIR/$IMAGE_FILE,if=none,index=0,media=disk,format=raw,id=disk0" -device "virtio-blk-pci,drive=disk0,disable-modern=on,disable-legacy=off" -dtb qemu-rpi-kernel/versatile-pb-buster-5.4.51.dtb -kernel qemu-rpi-kernel/kernel-qemu-5.4.51-buster -append 'root=/dev/vda2 panic=1' -no-reboot
