# 🏇 HorseRace Plugin

> Plugin Paper 1.21.1 — Course à cheval avec checkpoints, classement en temps réel et menu latéral.

---

## 📋 Sommaire

- [Fonctionnalités](#-fonctionnalités)
- [Installation](#-installation)
- [Compilation](#-compilation)
- [Configuration rapide d'une course](#-configuration-rapide-dune-course)
- [Commandes](#-commandes)
- [Permissions](#-permissions)
- [Scoreboard latéral](#-scoreboard-latéral)
- [Fichier config.yml](#-fichier-configyml)
- [Fichier de données checkpoints.yml](#-fichier-de-données-checkpointsyml)
- [FAQ](#-faq)

---

## ✨ Fonctionnalités

| Fonctionnalité | Description |
|---|---|
| 🚩 Checkpoints | Placez autant de checkpoints que vous voulez sur le parcours |
| 🔄 Retour checkpoint | `/checkpoint` téléporte au checkpoint précédent |
| 📊 Scoreboard latéral | Affiche en temps réel : checkpoint en cours, progression, temps, classement top 3 |
| 🏆 Classement | Classement live trié par checkpoints validés puis par temps |
| 💾 Sauvegarde | Les checkpoints sont sauvegardés dans `checkpoints.yml` au redémarrage |
| ⚙️ Config | Messages, rayon de détection et intervalles personnalisables |

---

## 🔧 Installation

1. Téléchargez le fichier `.jar` dans le dossier `/plugins/` de votre serveur Paper.
2. Redémarrez ou rechargez le serveur (`/reload confirm`).
3. Le dossier `plugins/HorseRace/` est créé automatiquement avec `config.yml`.

**Prérequis :**
- Paper 1.21.1 ou supérieur
- Java 21+

---

## 🛠️ Compilation

```bash
git clone <votre-repo>
cd HorseRace
mvn clean package
```

Le `.jar` se trouve dans `target/HorseRace-1.0.0.jar`.

---

## 🗺️ Configuration rapide d'une course

Voici les étapes pour créer une course de zéro :

### 1. Définir le point de départ
Placez-vous à l'endroit souhaité et tapez :
```
/setcheckpoint debut
```
ou avec des coordonnées précises :
```
/setcheckpoint debut 100 64 200
```

### 2. Définir les checkpoints intermédiaires
Numérotez-les dans l'ordre du parcours :
```
/setcheckpoint 1
/setcheckpoint 2
/setcheckpoint 3
```

### 3. Définir l'arrivée
```
/setcheckpoint fin
```

### 4. Démarrer la course
```
/startrace
```

Tous les joueurs montés sur un cheval au moment du départ sont automatiquement inscrits.  
Les autres peuvent rejoindre en montant sur un cheval pendant la course.

### 5. Arrêter la course
```
/stoprace
```

---

## 📝 Commandes

### Commandes joueurs

| Commande | Alias | Description |
|---|---|---|
| `/checkpoint` | `/cp` | Téléporte au checkpoint précédent |

### Commandes administrateurs (permission `horserace.admin`)

| Commande | Description |
|---|---|
| `/setcheckpoint <numéro\|debut\|fin> [x] [y] [z]` | Définit un checkpoint. Sans coordonnées = position actuelle |
| `/startrace` | Lance la course |
| `/stoprace` | Arrête la course |

#### Exemples `/setcheckpoint`

```bash
# Position actuelle du joueur
/setcheckpoint debut
/setcheckpoint 1
/setcheckpoint 2
/setcheckpoint fin

# Coordonnées manuelles
/setcheckpoint 1 150.5 64.0 -200.0
/setcheckpoint fin 500 70 100
```

---

## 🔐 Permissions

| Permission | Description | Défaut |
|---|---|---|
| `horserace.admin` | Accès aux commandes d'admin (set, start, stop) | OP |
| `horserace.play` | Participer à la course | Tous |

---

## 📊 Scoreboard latéral

Le scoreboard s'affiche automatiquement sur le côté droit de l'écran pendant une course.

```
        🏇 Course à Cheval
        ──────────────────
        Objectif: Checkpoint 2
        ──────────────────
        CPs: 1/5
        [███░░░░░░░]
        ──────────────────
        Temps: 42.3s
        Position: 2/4
        ──────────────────
         Classement
        🥇 Steve   38.1s
        🥈 Alex    42.3s
        🥉 Notch   1cp
```

**Légende :**
- **Objectif** : prochain checkpoint à atteindre
- **CPs** : checkpoints normaux validés / total
- **Barre de progression** : visuelle de l'avancement
- **Temps** : temps écoulé depuis le départ
- **Position** : place dans le classement actuel
- **Classement** : top 3 en temps réel

---

## ⚙️ Fichier `config.yml`

```yaml
# Rayon de détection des checkpoints (en blocs)
checkpoint-radius: 5

# Activer les messages de debug
debug: false

# Intervalle de mise à jour du scoreboard (en ticks, 20 = 1s)
scoreboard-update-interval: 10

messages:
  prefix: "&6[&eHorseRace&6] &r"
  race-started: "&aLa course a démarré ! Bonne chance !"
  race-stopped: "&cLa course a été arrêtée."
  checkpoint-reached: "&aCheckpoint &e%checkpoint% &aatteint !"
  finish-reached: "&6&lARRIVÉE ! &eVous terminez %position% en &b%time% &esecondes !"
  teleported-back: "&eVous avez été téléporté au checkpoint &6%checkpoint%&e."
  # ...
```

### Variables disponibles dans les messages

| Variable | Message | Valeur |
|---|---|---|
| `%checkpoint%` | checkpoint-reached, teleported-back | Nom du checkpoint |
| `%position%` | finish-reached | Position finale (ex: 1er) |
| `%time%` | finish-reached | Temps en secondes |
| `%x%`, `%y%`, `%z%` | checkpoint-set, start-set, finish-set | Coordonnées |

---

## 💾 Fichier de données `checkpoints.yml`

Généré automatiquement dans `plugins/HorseRace/checkpoints.yml`.  
**Ne pas modifier manuellement sauf si vous savez ce que vous faites.**

```yaml
start:
  world: world
  x: 100.0
  y: 64.0
  z: 200.0
checkpoints:
  1:
    world: world
    x: 150.0
    y: 65.0
    z: 250.0
  2:
    world: world
    x: 200.0
    y: 64.0
    z: 300.0
finish:
  world: world
  x: 500.0
  y: 64.0
  z: 400.0
```

---

## ❓ FAQ

**Q : Les joueurs doivent-ils être sur un cheval avant le `/startrace` ?**  
R : Oui, les joueurs montés au moment du `/startrace` sont inscrits automatiquement. Les autres peuvent rejoindre en montant sur un cheval pendant la course.

**Q : Que se passe-t-il si un joueur descend de son cheval ?**  
R : Sa progression est conservée. Il peut remonter sur un cheval et le scoreboard réapparaît.

**Q : Les checkpoints sont-ils détectés automatiquement ?**  
R : Oui, dès qu'un joueur entre dans le rayon d'un checkpoint (configurable dans `checkpoint-radius`), il est validé.

**Q : Puis-je utiliser `/checkpoint` pour revenir au départ ?**  
R : Oui, si vous êtes au premier checkpoint, `/checkpoint` vous ramène au départ.

**Q : Les checkpoints persistent-ils après un redémarrage ?**  
R : Oui, ils sont sauvegardés automatiquement dans `checkpoints.yml`.

---

## 📄 Licence

MIT — Libre d'utilisation, de modification et de distribution.

---

*Fait avec ❤️ pour les serveurs Minecraft Paper 1.21.1*
