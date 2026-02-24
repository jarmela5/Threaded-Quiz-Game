package GUI_cliente;



import GameState.PlacarMensagem;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import java.util.List;
import java.util.Comparator;

public class GUI_cliente_placar extends JFrame {

    //private final JTextArea placarArea = new JTextArea();

    public GUI_cliente_placar(PlacarMensagem placar, JFrame menu) {
        super("Placar - IsKahoot");


        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);


        //Cores Fundo:
        Color fundo = new Color(245, 247, 250);
        Color painelBranco=Color.WHITE;
        Color corTitulo = new Color(66, 133, 244);
        Color corTexto = new Color(44, 62, 80);
        Font fonteTitulo = new Font("Segoe UI", Font.BOLD, 24);
        Font fonteEquipa = new Font("Segoe UI", Font.BOLD, 16);
        Font fontePontos = new Font("Segoe UI", Font.PLAIN, 15);

        //Painel Principal
        JPanel painelPrincipal = new JPanel();
        painelPrincipal.setBackground(fundo);
        painelPrincipal.setLayout(new BoxLayout(painelPrincipal, BoxLayout.Y_AXIS));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        //titulo
        JLabel titulo = new JLabel("Classificação atual");
        titulo.setFont(fonteTitulo);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.setForeground(corTitulo);
        painelPrincipal.add(titulo);
        painelPrincipal.add(Box.createVerticalBox());


        //painel lista de equipas:
        JPanel painelListaEquipas = new JPanel();
        painelListaEquipas.setBackground(painelBranco);
        painelListaEquipas.setLayout(new BoxLayout(painelListaEquipas, BoxLayout.Y_AXIS));
        painelListaEquipas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Ordena as equipas por pontuação decrescente
        List<Map.Entry<String, Integer>> listaOrdenada = placar.getPlacar().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .toList();

        // Gera o placar visual
        int pos = 1;
        for (Map.Entry<String, Integer> entry : listaOrdenada) {
            String equipa = entry.getKey();
            int pontos = entry.getValue();

            JLabel label = new JLabel(String.format("%dº  %s — %d pontos", pos, equipa, pontos));
            label.setFont(fonteEquipa);
            label.setForeground(corTexto);

            // Destaques para os 3 primeiros
            if (pos == 1) label.setForeground(new Color(218, 165, 32));   // dourado
            else if (pos == 2) label.setForeground(new Color(160, 160, 160)); // prata
            else if (pos == 3) label.setForeground(new Color(205, 127, 50));  // bronze

            label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            painelListaEquipas.add(label);
            pos++;
        }

        // Se não houver equipas
        if (placar.getPlacar().isEmpty()) {
            JLabel vazio = new JLabel("Sem equipas ainda");
            vazio.setFont(fontePontos);
            vazio.setForeground(Color.GRAY);
            vazio.setAlignmentX(Component.CENTER_ALIGNMENT);
            painelListaEquipas.add(vazio);
        }

        // Scroll e montagem
        JScrollPane scroll = new JScrollPane(painelListaEquipas);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(painelBranco);

        painelPrincipal.add(scroll);
        painelPrincipal.add(Box.createVerticalStrut(15));

        //botão voltar
        JButton voltarBtn = new JButton();

        voltarBtn.setText("Sair");


        voltarBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        voltarBtn.setBackground(new Color(66, 133, 244));
        voltarBtn.setForeground(Color.WHITE);
        voltarBtn.setFocusPainted(false);
        voltarBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        voltarBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        voltarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        voltarBtn.addActionListener(e -> {
            dispose(); // fecha esta janela

        });


        painelPrincipal.add(voltarBtn);


        add(painelPrincipal);

        setVisible(true);
    }

}