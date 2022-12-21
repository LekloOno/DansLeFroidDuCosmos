<Mettre ici le nom de votre jeu>
===========

Développé par shems petremand, pierre bordone
Contacts : shems.petremand.etu@univ-lille.fr, pierre.bordone.etu@univ-lille.fr

# Présentation de "Dans le froid du cosmos"

Réveillé d'une longue hibernation, vous réalisez que vous êtes le dernier être humain en vie à bord du vaisseau.
Vous n'avez plus aucun souvenir de votre périple.
Vous devrez gérer intelligemment vos resources, et progresser dans l'espace avec stratégie pour parvenir à survivre, en apprendre un peu plus sur ce qu'il s'est passé, et peut-être retrouver la civilisation.



Pour l'instant, seul la programation du moteur d'affichage de fenêtre ascii, et la génération de système a été complété.
Ce que vous observerez en lançant le programme est un test de l'affichage de fenêtre réalisé avec 4 systèmes générés.
A terme, ce système d'affichage servira à afficher différents module d'hud, à savoir le système en haut à gauche, puis des module d'information, d'actions et d'historique.

Il reste encore à gérer les différentes interactions, et la progression dans le jeu.


Des captures d'écran illustrant le fonctionnement du logiciel sont proposées dans le répertoire shots.


# Utilisation de Dans le froid du cosmos

Afin d'utiliser le projet, il suffit de taper les commandes suivantes dans un terminal :

```
./compile.sh
```
Permet la compilation des fichiers présents dans 'src' et création des fichiers '.class' dans 'classes'

```
./run.sh Main
```
Permet le lancement du jeu
