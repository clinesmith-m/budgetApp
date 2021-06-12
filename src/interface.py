import tkinter as tk
import tkinter.ttk as ttk

class GUI(tk.Tk):
    def __init__(self, parent, categories):
        tk.Tk.__init__(self, parent)
        self.parent = parent
        self.categories = categories
        self.setScreen()

    def setScreen(self):
        frame = ttk.Frame(self, padding="3 3 12 12")
        frame.grid(column=0, row=0)#, sticky=(N, W, E, S))
        frame.columnconfigure(0, weight=1)
        frame.rowconfigure(0, weight=1)
        self.resizable(True, True)

        # Displaying all the budget categories
        catIndex = 1
        for cat in self.categories:
            labelText = cat[0] + "\n" + str(round(cat[1], 2))\
                            + "\n" + str(round(cat[2], 2)) 
            currLabel = tk.Label(
                text=labelText, 
                background="white", 
                relief="raised", 
                width=24
            )
            currLabel.grid(row=catIndex, column=5, pady=5, padx=8)
            catIndex += 1


if __name__ == "__main__":
    app = GUI(None, [("Hentai", 5000.00, 200.69), ("Johnny Sins Videos", 10000.00, 0.99)])
    app.title("STI Expense Tracker")
    app.mainloop()
