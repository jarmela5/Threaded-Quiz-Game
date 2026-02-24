package GUI_cliente;

import javax.swing.*;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.Map;
import java.util.List;

import GameState.*;
import perguntas.Question;

//fazer inicio, onde o cliente escolhe o tamanho e isso tudo

public class GUI_cliente_responder extends JFrame {

    private final JTextArea perguntaArea = new JTextArea("à espera dos outros jogadores");
    private final JRadioButton[] opcoes = new JRadioButton[4];
    private final ButtonGroup grupoOpcoes = new ButtonGroup();
    private final JLabel timerLabel = new JLabel("Tempo: 30s", SwingConstants.CENTER);

    private JButton submeterButton;
    private JPanel painelPlacar;
    
    private JScrollPane scrollPergunta;







    private ObjectInputStream in;
    private ObjectOutputStream out;



    private final String username;
    private final String codigoEquipa;


    public GUI_cliente_responder(String codeEquip, String userName, ObjectInputStream in, ObjectOutputStream out) {
        super("Apresentar perguntas");
        //this.game = game;
        this.username = userName;
        this.codigoEquipa = codeEquip;
        this.in = in;
        this.out = out;


        setDefaultCloseOperation(EXIT_ON_CLOSE);


        //Cores e fontes
        Color corFundo = new Color(245, 247, 250);
        Color corCartao = new Color(255, 255, 255);
        Color corBotao = new Color(66, 133, 244);
        Color corBotaoHover = new Color(52, 103, 190);
        Font fonteTitulo = new Font("Segoe UI", Font.BOLD, 20);
        Font fonteTexto = new Font("Segoe UI", Font.PLAIN, 16);




        //painel pergunta
        JPanel painelPergunta = new JPanel(new BorderLayout(10, 10));
        painelPergunta.setBackground(corCartao);
        painelPergunta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));


        perguntaArea.setEditable(false);
        perguntaArea.setLineWrap(true);
        perguntaArea.setWrapStyleWord(true);
        perguntaArea.setOpaque(false); // fundo transparente
        perguntaArea.setFont(fonteTitulo);
        perguntaArea.setForeground(new Color(66, 133, 244));
        perguntaArea.setFocusable(false);
        perguntaArea.setBorder(null);

        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        timerLabel.setForeground(new Color(120, 120, 120));


        JPanel headers = new JPanel(new GridLayout(2, 1));
        headers.setBackground(corCartao);
        headers.add(perguntaArea);
        headers.add(timerLabel);
        painelPergunta.add(headers, BorderLayout.NORTH);


        //opcoes ou resposta
        JPanel opcoesPanel = new JPanel(new GridLayout(4, 1, 8, 8));
        opcoesPanel.setBackground(corCartao);
        for (int i = 0; i < 4; i++) {
            opcoes[i] = new JRadioButton();
            opcoes[i].setFont(fonteTexto);
            opcoes[i].setBackground(Color.WHITE);
            grupoOpcoes.add(opcoes[i]);
            opcoesPanel.add(opcoes[i]);
        }

        painelPergunta.add(opcoesPanel, BorderLayout.CENTER);


        scrollPergunta = new JScrollPane(
                painelPergunta,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPergunta.setBorder(null);
        

        //botões
        JPanel botoesPanel = new JPanel();
        botoesPanel.setBackground(corFundo);


        submeterButton = new JButton("Submeter");
        JButton[] botoes = {submeterButton};

        for(JButton b : botoes){
            b.setFont(new Font("Segoe UI", Font.BOLD, 15));
            b.setBackground(corBotao);
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.setPreferredSize(new Dimension(140, 45));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            botoesPanel.add(b);

            //efeito houver
            b.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    b.setBackground(corBotaoHover);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    b.setBackground(corBotao);
                }
            });
        }


        JPanel painelPerguntasContainer = new JPanel(new BorderLayout(10, 10));
        painelPerguntasContainer.add(scrollPergunta, BorderLayout.CENTER);
        painelPerguntasContainer.add(botoesPanel, BorderLayout.SOUTH);

        painelPlacar = new JPanel();
        painelPlacar.setLayout(new BorderLayout());
        painelPlacar.setBackground(new Color(245, 247, 250));


        JLabel tituloPlacar = new JLabel("Classificação atual", SwingConstants.CENTER);
        tituloPlacar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tituloPlacar.setForeground(new Color(66, 133, 244));
        painelPlacar.add(tituloPlacar, BorderLayout.NORTH);
        painelPlacar.setMinimumSize(new Dimension(280, 0));
        painelPerguntasContainer.setMinimumSize(new Dimension(550, 0));
       


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelPerguntasContainer, painelPlacar);

        splitPane.setResizeWeight(0.65); // 65% perguntas, 35% placar
        //splitPane.setDividerLocation(500);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(6);
        splitPane.setOneTouchExpandable(false);


        add(splitPane);

        setMinimumSize(new Dimension(850, 450));
        setExtendedState(JFrame.NORMAL);


        setLocationRelativeTo(null);
        setVisible(true);




        submeterButton.addActionListener(e -> {
            try {
                submeterResposta();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });


        new Thread(()-> {
            boolean running=true;
            while (running) {
                try {

                    Object obj = in.readObject();
                    if (obj instanceof String msg && msg.equals("FIM")) {
                        running = false;

                        SwingUtilities.invokeLater(this::dispose);
                        break;
                    } else if (obj instanceof Question) {
                        Question q = (Question) obj;
                        // Atualiza a GUI no thread certo
                        SwingUtilities.invokeLater(() -> mostrarPergunta(q));
                    } else if (obj instanceof Integer) {
                        receverTempo((int) obj);



                    }else if(obj instanceof PlacarMensagem){


                        SwingUtilities.invokeLater(() -> atualizarPlacarLateral(((PlacarMensagem) obj)));
                    }
                    else
                        System.out.println("Objeto inesperado recebido: " + obj);

                }catch (IOException  e){
                    System.out.println("Conexão perdida com o servidor.");
                    running = false;
                    SwingUtilities.invokeLater(this::dispose);

                } catch (ClassNotFoundException  e) {
                    e.printStackTrace();

                }

            }
        }).start();

    }



    private void receverTempo(long tempo) throws IOException {
        timerLabel.setText("Tempo: " + tempo + "s");
        if(tempo==1){

            out.writeObject("timeout");
            out.flush();
        }
    }


    private void mostrarPergunta(Question q) {
        submeterButton.setEnabled(true);  // na nova pergunta

        perguntaArea.setText(q.getQuestion());

        for (int i = 0; i < 4; i++) {
            opcoes[i].setText(q.getOptions().get(i));
        }
        //desmarca todas as opcoes
        grupoOpcoes.clearSelection();

    }


    private void submeterResposta() throws IOException {

        if(!perguntaArea.getText().trim().isEmpty() && !perguntaArea.getText().trim().equals("à espera dos outros jogadores")) {
            if (grupoOpcoes.getSelection() == null) {
                JOptionPane.showMessageDialog(this, "Selecione uma opção!");
                return;
            }



            // Descobre qual opção foi selecionada para depois saber qual enviar
            int opcaoSelecionada = -1;
            for (int i = 0; i < opcoes.length; i++) {
                if (opcoes[i].isSelected()) {
                    opcaoSelecionada = i;

                    break;
                }
            }


            if (opcaoSelecionada == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma opção!!");
                return;
            }

            Resposta resposta = new Resposta(opcaoSelecionada, 0, username);
            try {


                out.writeObject(resposta);
                out.flush();
                submeterButton.setEnabled(false); // após submeter
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void bloquearResposta() {
        for (JRadioButton opcao : opcoes) {
            opcao.setEnabled(false);
        }
    }

    private void desbloquearResposta() {
        for (JRadioButton opcao : opcoes) {
            opcao.setEnabled(true);
        }
        grupoOpcoes.clearSelection();
    }




    private void atualizarPlacarLateral(PlacarMensagem placarMsg) {
        if (placarMsg == null) return;

        Map<String, Integer> placarTotal = placarMsg.getPlacar();
        Map<String, Integer> pontosUltimaPergunta = placarMsg.getPlacarAntigo();

        JPanel lista = new JPanel();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBackground(Color.WHITE);
        lista.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        List<Map.Entry<String, Integer>> listaOrdenada = placarTotal.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .toList();

        int pos = 1;
        for (Map.Entry<String, Integer> entry : listaOrdenada) {
            String teamId = entry.getKey();
            int total = entry.getValue();
            int pontosUltima = pontosUltimaPergunta.getOrDefault(teamId, 0);

            JLabel label = new JLabel(String.format("%dº  %s — %d pontos (+%d)", pos, teamId, total, pontosUltima));
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            if (pos == 1) label.setForeground(new Color(218, 165, 32));
            else if (pos == 2) label.setForeground(new Color(160, 160, 160));
            else if (pos == 3) label.setForeground(new Color(205, 127, 50));
            else label.setForeground(new Color(44, 62, 80));
            //label.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
            label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));


            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height));

            lista.add(label);
            pos++;
        }

        lista.setPreferredSize(null);

        painelPlacar.removeAll();
        JLabel titulo = new JLabel("Classificação atual", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(new Color(66, 133, 244));
        painelPlacar.add(titulo, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(lista);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16); // scroll mais suave

        painelPlacar.add(scroll, BorderLayout.CENTER);


        painelPlacar.revalidate();
        painelPlacar.repaint();
    }









}




