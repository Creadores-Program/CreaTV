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
    { name: 'Matias01jr (Minecraft, Roblox) (Creador del programa original)', platform: 'twitch', tag: 'matias01jr' },
    { name: 'Matias01jr (Minecraft, Roblox) (Creador del programa original)', platform: 'kick', tag: 'matias01jr' },
    { name: 'Matias01jr (Minecraft, Roblox) (Creador del programa original)', platform: 'youtube', tag: 'matias01jr' },
    { name: 'Matias01jr (Minecraft, Roblox) (Creador del programa original)', platform: 'tiktok', tag: 'matias01gamer' },
    { name: 'Trollhunters501 (Roblox, Minecraft, Programing) (Creador de la app)', platform: 'twitch', tag: 'trollhunters501' },
    { name: 'Trollhunters501 (Programing) (Creador de la app)', platform: 'kick', tag: 'trollhunters501' },
    { name: 'Trollhunters501 (Roblox, Minecraft) (Creador de la app)', platform: 'youtube', tag: 'Trollhunters501' },
    { name: 'katynwn (ASMR, Karaoke, Games)', platform: 'twitch', tag: 'katynwn'},
    { name: 'lSoyDarki (LOL, ASMR, Karaoke)', platform: 'twitch', tag: 'lsoydarki'},
    { name: 'Bobicraft (Minecraft)', platform: 'twitch', tag: 'bobicraftmc'},
    { name: 'VicmasterPE (Craftsman, Minecraft)', platform: 'twitch', tag: 'vicmaster_pe'},
    { name: 'Vicmaster (Craftsman, Minecraft)', platform: 'youtube', tag: 'vicmaster_'},
    { name: 'Ibai', platform: 'twitch', tag: 'ibai' },
    { name: 'ElXokas', platform: 'twitch', tag: 'elxokas' },
    { name: 'WestCOL', platform: 'kick', tag: 'westcol' },
    { name: 'AuronPlay', platform: 'twitch', tag: 'auronplay' },
    { name: 'El Rubius (Gaming, Variety)', platform: 'twitch', tag: 'elrubius' },
    { name: 'Khaby Lame (Humor)', platform: 'tiktok', tag: 'khaby.lame' },
    { name: 'Alexis Omman (Challenges and Gifts)', platform: 'tiktok', tag: 'alexisomman' },
    { name: 'Carlos Feria (Entertainment)', platform: 'tiktok', tag: 'carlosferia4' },
    { name: 'El Magnum! (Minecraft, Roblox, Games)', platform: 'tiktok', tag: 'magnumyoutube' },
    { name: 'El Magnum (Minecraft, Roblox, Games)', platform: 'youtube', tag: 'ElMagnum' },
    { name: 'Plech (Roblox, Minecraft, Games)', platform: 'youtube', tag: 'Plech' }
  ];

  var platformBaseUrls = {
    twitch: 'https://twitch.tv/',
    kick: 'https://kick.com/',
    youtube: 'https://youtube.com/@',
    tiktok: 'https://www.tiktok.com/@'
  };

  var STORAGE_KEY = 'saved_stream_creators';

  var qualitySelect = document.getElementById('stream-quality');
  var featuredContainer = document.getElementById('creadores-destacados');
  var savedContainer = document.getElementById('creadores-guardados');
  
  var customForm = document.getElementById('custom-creator-form');
  var nametagInput = document.getElementById('nametag');
  var saveCheckbox = document.getElementById('save-creator');
  var openChatCheck = document.getElementById('open-chat');

  
  function isLowQualitySelected() {
    return qualitySelect.value === 'low';
  }

  function buildStreamUrl(platform, tag) {
    var cleanTag = tag.replace(/^\s+|\s+$/g, '').replace(/^@/, '');
    var platKey = platform.toLowerCase();
    if (platKey === 'youtube' || platKey === 'tiktok') {
      return platformBaseUrls[platKey] + cleanTag + '/live';
    }
    var baseUrl = platformBaseUrls[platKey] ? platformBaseUrls[platKey] : platformBaseUrls['twitch'];
    
    return baseUrl + cleanTag;
  }

  function openStreamInAndroid(url) {
    var lowQuality = isLowQualitySelected();
    var openChat = openChatCheck.checked && url.indexOf("twitch") >= 0;
    Android.openVideo(url, lowQuality, openChat);
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

  function deleteCreatorFromStorage(tag, platform) {
    var savedRaw = localStorage.getItem(STORAGE_KEY);
    var saved = savedRaw ? JSON.parse(savedRaw) : [];
    var updated = [];
    var i;

    for (i = 0; i < saved.length; i++) {
      if (!(saved[i].tag.toLowerCase() === tag.toLowerCase() && saved[i].platform === platform)) {
        updated.push(saved[i]);
      }
    }

    localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
    renderSavedCreators();
  }

  function createCreatorCard(creator, isRemovable) {
    var card = document.createElement('div');
    card.className = 'creator-card platform-badge-' + creator.platform;
    card.style.cssText = 'background: #18181c; border: 1px solid #282830; border-radius: 8px; padding: 0.75rem 1rem; display: flex; align-items: center; justify-content: space-between; gap: 1rem; cursor: pointer; min-width: 160px; position: relative;';

    var displayName = creator.name ? creator.name : creator.tag;

    var deleteBtnHtml = '';
    if (isRemovable) {
      deleteBtnHtml = '<button type="button" class="btn-delete-creator" style="background:transparent; border:none; color:#ff4d4d; font-weight:bold; font-size:1rem; cursor:pointer; padding:0 0 0 0.5rem; line-height:1;">&#10005;</button>';
    }

    card.innerHTML = '<div style="flex-grow:1;">' +
      '<strong style="display:block; color:#fff;">' + displayName + '</strong>' +
      '<small style="color:#8a8a9e; text-transform:uppercase; font-size:0.7rem;">' + creator.platform + '</small>' +
    '</div>' + deleteBtnHtml;

    card.onclick = function() {
      var url = buildStreamUrl(creator.platform, creator.tag);
      openStreamInAndroid(url);
    };

    if (isRemovable) {
      var btnDelete = card.getElementsByTagName('button')[0];
      if (btnDelete) {
        btnDelete.onclick = function(e) {
          var event = e || window.event;
          if (event.stopPropagation) {
            event.stopPropagation();
          } else {
            event.cancelBubble = true;
          }
          
          deleteCreatorFromStorage(creator.tag, creator.platform);
        };
      }
    }

    return card;
  }

  function renderFeaturedCreators() {
    featuredContainer.innerHTML = '';
    var i;
    for (i = 0; i < defaultFeaturedCreators.length; i++) {
      var card = createCreatorCard(defaultFeaturedCreators[i], false);
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
      var card = createCreatorCard(saved[i], true);
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
function toggleAboutMenu() {
    var content = document.getElementById("about-content");
    var icon = document.getElementById("about-icon");

    if (!content) return;

    if (content.className.indexOf("about-expanded") > -1) {
        content.className = "about-body about-collapsed";
        if (icon) icon.innerHTML = "▼";
    } else {
        content.className = "about-body about-expanded";
        if (icon) icon.innerHTML = "▲";
    }
}
