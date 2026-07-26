window.langPage = JSON.parse(Android.getLangJson());
if (!String.prototype.startsWith) {
    String.prototype.startsWith = function(search, pos) {
        pos = pos || 0;
        return this.substring(pos, pos + search.length) === search;
    };
}
if (!String.prototype.trim) {
    String.prototype.trim = function() {
        return this.replace(/^\s+|\s+$/g, '');
    };
}

window.onload = function() {

  var defaultFeaturedCreators = [
    { name: 'Ibai', platform: 'twitch', tag: 'ibai' },
    { name: 'ElXokas', platform: 'twitch', tag: 'elxokas' },
    { name: 'WestCOL', platform: 'kick', tag: 'westcol' },
    { name: 'AuronPlay', platform: 'twitch', tag: 'auronplay' }
  ];

  var platformBaseUrls = {
    twitch: 'https://twitch.tv/',
    kick: 'https://kick.com/',
    youtube: 'https://youtube.com/@'
  };

  var STORAGE_KEY = 'saved_stream_creators';

  var qualitySelect = document.getElementById('stream-quality');
  var featuredContainer = document.getElementById('creadores-destacados');
  var savedContainer = document.getElementById('creadores-guardados');
  
  var customForm = document.getElementById('custom-creator-form');
  var nametagInput = document.getElementById('nametag');
  var saveCheckbox = document.getElementById('save-creator');

  
  function isLowQualitySelected() {
    return qualitySelect.value === 'low';
  }

  function buildStreamUrl(platform, tag) {
    var cleanTag = tag.replace(/^\s+|\s+$/g, '').replace(/^@/, '');
    var platKey = platform.toLowerCase();
    var baseUrl = platformBaseUrls[platKey] ? platformBaseUrls[platKey] : platformBaseUrls['twitch'];
    
    return baseUrl + cleanTag;
  }

  function openStreamInAndroid(url) {
    var lowQuality = isLowQualitySelected();
    
    if (typeof Android !== 'undefined' && typeof Android.openVideo === 'function') {
      Android.openVideo(url, lowQuality);
    }
  }

  function creatorExists(array, tag, platform) {
    var i;
    for (i = 0; i < array.length; i++) {
      if (array[i].tag.toLowerCase() === tag.toLowerCase() && array[i].platform === platform) {
        return true;
      }
    }
    return false;
  }

  function createCreatorCard(creator) {
    var card = document.createElement('div');
    card.className = 'creator-card platform-badge-' + creator.platform;
    card.style.cssText = 'background: #18181c; border: 1px solid #282830; border-radius: 8px; padding: 0.75rem 1rem; display: flex; align-items: center; justify-content: space-between; gap: 1rem; cursor: pointer; min-width: 160px;';

    var displayName = creator.name ? creator.name : creator.tag;

    card.innerHTML = '<div>' +
      '<strong style="display:block; color:#fff;">' + displayName + '</strong>' +
      '<small style="color:#8a8a9e; text-transform:uppercase; font-size:0.7rem;">' + creator.platform + '</small>' +
    '</div>';

    card.onclick = function() {
      var url = buildStreamUrl(creator.platform, creator.tag);
      openStreamInAndroid(url);
    };

    return card;
  }

  function renderFeaturedCreators() {
    featuredContainer.innerHTML = '';
    var i;
    for (i = 0; i < defaultFeaturedCreators.length; i++) {
      var card = createCreatorCard(defaultFeaturedCreators[i]);
      featuredContainer.appendChild(card);
    }
  }

  function renderSavedCreators() {
    savedContainer.innerHTML = '';
    
    var savedRaw = localStorage.getItem(STORAGE_KEY);
    var saved = savedRaw ? JSON.parse(savedRaw) : [];

    if (saved.length === 0) {
      savedContainer.innerHTML = '<span style="color:#4a4a5a; font-size:0.85rem; font-style:italic;">'+window.langPage.noCreadores+'</span>';
      return;
    }

    var i;
    for (i = 0; i < saved.length; i++) {
      var card = createCreatorCard(saved[i]);
      savedContainer.appendChild(card);
    }
  }

  function saveCreatorToStorage(newCreator) {
    var savedRaw = localStorage.getItem(STORAGE_KEY);
    var saved = savedRaw ? JSON.parse(savedRaw) : [];
    
    if (!creatorExists(saved, newCreator.tag, newCreator.platform)) {
      saved.push(newCreator);
      localStorage.setItem(STORAGE_KEY, JSON.stringify(saved));
      renderSavedCreators();
    }
  }

  customForm.onsubmit = function(event) {
    if (event && event.preventDefault) {
      event.preventDefault();
    } else if (window.event) {
      window.event.returnValue = false;
    }

    var tag = nametagInput.value.replace(/^\s+|\s+$/g, '');
    if (!tag) return false;

    var platformInputs = document.getElementsByName('platform');
    var platform = 'twitch';
    var i;

    for (i = 0; i < platformInputs.length; i++) {
      if (platformInputs[i].checked) {
        platform = platformInputs[i].value;
        break;
      }
    }

    var streamUrl = buildStreamUrl(platform, tag);

    if (saveCheckbox.checked) {
      saveCreatorToStorage({
        name: tag,
        tag: tag,
        platform: platform
      });
      saveCheckbox.checked = false;
    }

    openStreamInAndroid(streamUrl);
    return false;
  };

  renderFeaturedCreators();
  renderSavedCreators();
  var elementsQlang = document.querySelectorAll("[langId]");
    for(var idod = 0; idod < elementsQlang.length; idod++){
        var elementQlang = elementsQlang[idod];
        var attrLang = elementQlang.getAttribute("langId");
        if(window.langPage[attrLang]){
            elementQlang.textContent = window.langPage[attrLang];
        }else{
            console.warn("Invalid key " + attrLang);
        }
    }
    document.body.style.opacity = "1";
};