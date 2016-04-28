function initMap() {
  var cmuLatLng = {lat: 40.4425, lng: -79.9426};

  var map = new google.maps.Map(document.getElementById('map'), {
    zoom: 12,
    center: cmuLatLng
  });

  var obj1 = {lat: 40.4425, lng: -79.9426}
  var obj2 = {lat: 41.4425, lng: -78.9426}
  var obj3 = {lat: 42.4425, lng: -80.9426}
  
  var latlngs = new Array(obj1, obj2, obj3);
  var markers = new Array(latlngs.length)
  for (i = 0; i < latlngs.length; i++) {
    markers[i] = new google.maps.Marker({
                   position: latlngs[i],
                   map: map,
                   title: 'Click to zoom'
                 });
    var f = function(i) {
      return function() {
        map.setZoom(14);
        map.setCenter(markers[i].getPosition());
      }
    };
    markers[i].addListener('click', f(i));
  }
}
