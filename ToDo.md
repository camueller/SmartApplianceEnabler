# Erweiterung HttpEnergieMeter für Enegiemengen
Man kann sie auch aus den Differenzen der Energiemengen und der Zeitdifferenz berechnen, wobei man in diesem Fall vermutlich eine Zeitdifferenz von z.B. 30s wählen würde. Wenn ich also im SAE den HTTP-basierten Zähler zur Unterstützung von Energiemengen anpasse, kann man entweder URLs für Leistung und Energiemenge angeben und direkt abfragen, oder nur eine URL für die Energiemenge angeben und dann die Leistung berechnen lassen.

Dienlich ist dabei auch, dass der go-eCharger die Energiemenge sehr genau misst.