alter table QUOTE add USED BOOLEAN default 0;
update QUOTE set USED = 1 where id in (select QUOTE from WALLPAPER);