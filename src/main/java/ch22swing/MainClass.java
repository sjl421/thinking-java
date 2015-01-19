/*
 * Created on 13-6-22
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright @2013 the original author or authors.
 */
package ch22swing;

/**
 * Description of this file.
 *
 * @author XiongNeng
 * @version 1.0
 * @since 13-6-22
 */

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainClass {

    public static void main(String[] a) {
        JFrame frame = new JFrame("Alignment Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String labels[] = {"--", "----", "--------", "------------"};

        JPanel container = new JPanel();
        BoxLayout layout = new BoxLayout(container, BoxLayout.Y_AXIS);
        container.setLayout(layout);

        for (int i = 0; i < labels.length; i++) {
            JButton button = new JButton(labels[i]);
            button.setAlignmentX(Component.RIGHT_ALIGNMENT);
            container.add(button);
        }

        frame.add(container, BorderLayout.CENTER);

        frame.setSize(300, 200);
        frame.setVisible(true);
    }
}
