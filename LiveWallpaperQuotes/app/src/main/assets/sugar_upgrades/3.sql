alter table WALLPAPER add TYPEFACE_ID INTEGER default 0;
update WALLPAPER set TYPEFACE_ID = 6 where id > 0;