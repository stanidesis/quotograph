$(function() {
  $('.peppermint').Peppermint({
    slideshow: true,
    speed: 1000,
    slideshowInterval: 3000,
    stopSlideshowAfterInteraction: true
  });
  var height = $(window).height();
  $('.quote-wrapper > .container').each(function(index) {
    var containerHeight = $(this).height();
    $(this).css('margin-top', (height - containerHeight) / 2);
  });
});