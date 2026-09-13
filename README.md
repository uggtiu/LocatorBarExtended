# 🧭 Extended Locator Bar

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-107C41?style=for-the-badge&logo=minecraft&logoColor=white)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-Loader%200.16+-black?style=for-the-badge&logo=fabric&logoColor=white)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

<!--
[![Modrinth](https://img.shields.io/badge/Modrinth-Available-00AF5C?style=flat-square&logo=modrinth&logoColor=white)](https://modrinth.com/mod/locatorbarextended)
[![CurseForge](https://img.shields.io/badge/CurseForge-Available-F16436?style=flat-square&logo=curseforge&logoColor=white)](https://curseforge.com/minecraft/mc-mods/locatorbarextended)
-->
[![GitHub Release](https://img.shields.io/github/v/release/uggtiu/LocatorBarExtended?style=flat-square&color=6f42c1&logo=github)](https://github.com/uggtiu/LocatorBarExtended/releases)

Клиентский мод для Minecraft 1.21.11, преображающий стандартную панель Locator Bar: скины игроков, настраиваемые плашки ников, гибкие профили и кастомные цвета.

---

## ✨ Основные возможности

* 👤 **Головы игроков со скином:** Замена стандартных цветных ромбиков на лица игроков с поддержкой объемного второго слоя (hat layer).
* 💬 **Улучшенный вид ника (Bubble Badge):** Стильная полупрозрачная плашка с направляющей стрелочкой вниз прямо к голове игрока.
* 🧹 **Режим «Чистые ники» (Clean Nicknames):** Убирает кастомные серверные префиксы, эмодзи, ранги и клан-теги (например, `🐉`), отображая чистое имя аккаунта игрока.
* ⭕ **Пиксельное закругление:** Настраиваемый срез углов головы (0% — квадрат, 50% — срез, 100% — круглый аватар).
* 🎯 **Индивидуальный хайлайт:** Цветная рамка вокруг головы и кастомный цвет плашки ника (по умолчанию `#FF0000`).
* 🎨 **Кастомный цвет иконок:** Возможность переключить конкретного игрока обратно в режим ванильного ромбика и перекрасить его в любой цвет.
* 🌐 **Каскадные скоупы (Server vs Global):** Настройки можно задавать глобально или изолированно для конкретного сервера/мира.
* 🤖 **Автозапоминание игроков:** Встреченные игроки автоматически регистрируются в конфиге со статусом следования глобальным настройкам.
* ⚙️ **Внутриигровой GUI:** Полная интеграция с **YetAnotherConfigLib (YACL v3)** и **Mod Menu**.
* 🌍 **Двуязычная локализация:** Полный перевод интерфейса и команд чата на **Русский** и **English**.

---

## 📸 Скриншоты

![In-game Screenshot1](https://raw.githubusercontent.com/uggtiu/LocatorBarExtended/main/screenshot1.jpg)
![In-game Screenshot2](https://raw.githubusercontent.com/uggtiu/LocatorBarExtended/main/screenshot2.png)

---

## ⌨️ Команды в чате (`/ble`)

| Команда | Описание |
| :--- | :--- |
| `/ble gui` | Открыть визуальное меню настроек |
| `/ble reload` | Перезагрузить конфиг с диска |
| `/ble global mode <head\|icon>` | Глобальный режим отображения |
| `/ble global clean_nicknames <true\|false>` | Включить/выключить чистые ники без серверных эмодзи |
| `/ble global head_size <4-24>` | Базовый размер головы |
| `/ble global rounding <0-100>` | Процент закругления голов |
| `/ble global highlight <true\|false> [цвет]` | Глобальная цветная обводка |
| `/ble global enhanced <true\|false>` | Переключение плашки Bubble / классического текста |
| `/ble player <ник> mode <head\|icon> [server\|global]` | Режим конкретного игрока |
| `/ble player <ник> icon_color <цвет> [server\|global]` | Цвет ванильного ромбика игрока |
| `/ble player <ник> highlight <true\|false> [цвет] [server\|global]` | Хайлайт конкретного игрока |
| `/ble player <ник> nickname_color <цвет> [server\|global]` | Цвет текста ника игрока |
| `/ble player <ник> reset [server\|global]` | Сбросить оверрайды игрока к настройкам Global |

> **Формат цветов:** поддерживаются HEX (`#FF0000`, `0x00FFCC`) и ключевые слова (`red`, `green`, `blue`, `gold`, `purple` и др.).

---

## 🛠️ Установка

1. Установите **[Fabric Loader](https://fabricmc.net/)** (версии `0.16.0+`).
2. Поместите в папку `.minecraft/mods`:
   * Сам мод **Extended Locator Bar**
   * **[Fabric API](https://modrinth.com/mod/fabric-api)**
   * **[YetAnotherConfigLib (YACL)](https://modrinth.com/mod/yacl)**
   * *(Опционально)* **[Mod Menu](https://modrinth.com/mod/modmenu)** для доступа к настройкам через список модов.

---

## 🔒 Честная игра и ванильное поведение

* Мод работает **строго на клиенте** и не требует установки на сервер.
* Если администратор выключил локатор (`/gamerule locator_bar false`), мод не отображает ничего.
* Игроки, скрытые ванильными механиками (приседание/sneak, невидимость, тыква на голове, режим зрителя), **не отображаются**. Никакого WH или нечестного преимущества.

---

## 📄 Лицензия

Проект распространяется под открытой лицензией **[MIT](LICENSE)**. Свободен для использования в любых сборках и модпаках.
