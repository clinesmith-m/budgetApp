import tkinter as tk
import tkinter.ttk as ttk

class GUI(tk.Tk):
    def __init__(self, parent, categories):
        tk.Tk.__init__(self, parent)
        self.parent = parent
        self.categories = categories
        self.setFrame()
        self.setInteractions()
        # Optimizing by breaking all of the visual elements that need to be 
        # queried for into seperate catagories so they can be updated separately
        self.catLogs = []
        self.setCatLogs()
        self.recExpLogs = []
        self.setRecExpLogs()
        self.recIncLogs = []
        self.setRecIncLogs()
        self.statLogs = []
        self.setStatLogs()


    def setFrame(self):
        self.frame = ttk.Frame(self, padding="3 3 12 12")
        self.frame.grid(column=0, row=0)#, sticky=(N, W, E, S))
        self.frame.columnconfigure(0, weight=1)
        self.frame.rowconfigure(0, weight=1)
        self.resizable(True, True)


    def setCatLogs(self):
        # Killing the widgets for the previous logs
        for widget in self.catLogs:
            widget.grid_forget()
        self.catLogs = []

        # Placing an overarching label over all the catLogs
        catIndex = 1
        sectionLabel = tk.Label(text="Spending Categories", width=32)
        sectionLabel.grid(row=catIndex, column=5, pady=5, padx=5)
        self.catLogs.append(sectionLabel)

        # Displaying all the budget categories
        catIndex += 1
        for cat in self.categories:
            labelText = cat[0] + "\n" + str(round(cat[1], 2))\
                            + "\n" + str(round(cat[2], 2)) 
            currLabel = tk.Label(
                text=labelText, 
                background="white", 
                relief="raised", 
                width=32
            )
            currLabel.grid(row=catIndex, column=5, pady=5, padx=8)
            self.catLogs.append(currLabel)
            catIndex += 1


    def setRecExpLogs(self):
        pass


    def setRecIncLogs(self):
        pass


    def setStatLogs(self):
        pass


    def setInteractions(self):
        # Creating the entry boxes for actual interactions

        # Starting with the Create new category field
        currRow=1
        tk.Label(
            text="Create/Modify Category",
            width=32
        ).grid(row=currRow, column=1, padx=5)

        tk.Label(
            text="Cat_Name-Amt",
            width=24
        ).grid(row=currRow, column=2, padx=5)

        currRow += 1
        self.catNameBox = tk.Entry(width=32)
        self.catNameBox.grid(row=currRow, column=1, pady=5, padx=5)

        self.catAmountBox = tk.Entry(width=10)
        self.catAmountBox.grid(row=currRow, column=2, pady=5, padx=5)

        self.modCatBtn = tk.Button(text="Modify", command=self.modCategory)
        self.modCatBtn.grid(row=currRow, column=3, pady=5, padx=5)

        self.addCatBtn = tk.Button(text="Add", command=self.addCategory)
        self.addCatBtn.grid(row=currRow, column=4, pady=5, padx=5)

        # Allowing user to report expenditures
        # One time expenditures first
        currRow += 1
        tk.Label(
            text="Report One Time Expenditure",
            width=32
        ).grid(row=currRow, column=1, padx=5)

        tk.Label(
            text="Cat_Name-Amt-Memo",
            width=24
        ).grid(row=currRow, column=2, padx=5)

        currRow += 1
        self.singleExpCat = tk.Entry(width=32)
        self.singleExpCat.grid(row=currRow, column=1, pady=5, padx=5)

        self.singleExpAmt = tk.Entry(width=10)
        self.singleExpAmt.grid(row=currRow, column=2, pady=5, padx=5)

        self.singleExpMemo = tk.Entry(width=32)
        self.singleExpMemo.grid(row=currRow, column=3, pady=5, padx=5)

        self.singleExpBtn = tk.Button(text="Report", command=self.reportSingleExp)
        self.singleExpBtn.grid(row=currRow, column=4, pady=5, padx=5)

        # Then recurring expenditures
        # To greatly simplify the design, recurring expenditures will be categories
        # unto themselves, rather than slotting into existing categories
        currRow += 1
        tk.Label(
            text="Create Monthly Expenditure",
            width=32
        ).grid(row=currRow, column=1, padx=5)

        tk.Label(
            text="Memo-Amt-Num_Months",
            width=24
        ).grid(row=currRow, column=2, padx=5)

        currRow += 1
        self.recExpMemo = tk.Entry(width=32)
        self.recExpMemo.grid(row=currRow, column=1, pady=5, padx=5)

        self.recExpAmt = tk.Entry(width=10)
        self.recExpAmt.grid(row=currRow, column=2, pady=5, padx=5)

        self.recExpLen = tk.Entry(width=4)
        self.recExpLen.grid(row=currRow, column=3, pady=5, padx=5)

        self.recExpBtn = tk.Button(text="Create", command=self.createRecExp)
        self.recExpBtn.grid(row=currRow, column=4, pady=5, padx=5)

        # Allowing user to cancel a monthly expense
        currRow += 1
        tk.Label(
            text="Cancel Monthly Expense",
            width=32
        ).grid(row=currRow, column=1, padx=5)

        tk.Label(
            text="Memo",
            width=24
        ).grid(row=currRow, column=2, padx=5)

        currRow += 1
        self.cancelExpMemo = tk.Entry(width=32)
        self.cancelExpMemo.grid(row=currRow, column=1, pady=5, padx=5)

        self.cancelExpBtn = tk.Button(text="Cancel", command=self.cancelRecExp)
        self.cancelExpBtn.grid(row=currRow, column=4, pady=5, padx=5)

        # Allowing user to create a monthly income
        currRow += 1
        tk.Label(
            text="Create Monthly Income",
            width=32
        ).grid(row=currRow, column=1, padx=5)

        tk.Label(
            text="Memo-Amt",
            width=24
        ).grid(row=currRow, column=2, padx=5)

        currRow += 1
        self.recIncMemo = tk.Entry(width=32)
        self.recIncMemo.grid(row=currRow, column=1, pady=5, padx=5)

        self.recIncAmt = tk.Entry(width=10)
        self.recIncAmt.grid(row=currRow, column=2, pady=5, padx=5)

        self.recIncBtn = tk.Button(text="Create", command=self.createRecInc)
        self.recIncBtn.grid(row=currRow, column=4, pady=5, padx=5)

        # And cancel monthly income
        currRow += 1
        tk.Label(
            text="Cancel Monthly Income",
            width=32
        ).grid(row=currRow, column=1, padx=5)

        tk.Label(
            text="Memo",
            width=24
        ).grid(row=currRow, column=2, padx=5)

        currRow += 1
        self.cancelIncMemo = tk.Entry(width=32)
        self.cancelIncMemo.grid(row=currRow, column=1, pady=5, padx=5)

        self.cancelIncBtn = tk.Button(text="Cancel", command=self.cancelRecInc)
        self.cancelIncBtn.grid(row=currRow, column=4, pady=5, padx=5)

        # Also letting the user report one-time income
        currRow += 1
        tk.Label(
            text="Report One Time Income",
            width=32
        ).grid(row=currRow, column=1, padx=5)

        tk.Label(
            text="Memo-Amt",
            width=24
        ).grid(row=currRow, column=2, padx=5)

        currRow += 1
        self.singleIncMemo = tk.Entry(width=32)
        self.singleIncMemo.grid(row=currRow, column=1, pady=5, padx=5)

        self.singleIncAmt = tk.Entry(width=10)
        self.singleIncAmt.grid(row=currRow, column=2, pady=5, padx=5)

        self.singleIncBtn = tk.Button(text="Report", command=self.reportSingleInc)
        self.singleIncBtn.grid(row=currRow, column=4, pady=5, padx=5)


        # Making an alert box at the bottom of the app to notify users of
        # changes in state
        currRow += 1
        self.alertText = tk.StringVar()
        alertBox = tk.Label(
            textvariable=self.alertText, 
            width=32
        )
        alertBox.grid(row=currRow, column=1, pady=5, padx=5)


    def modCategory(self):
        catName = self.catNameBox.get()
        self.alertText.set("Category modified")


    def addCategory(self):
        catName = self.catNameBox.get()
        self.alertText.set("Category added")


    def reportSingleExp(self):
        catName = self.singleExpCat.get()
        alertTxt = "Le text: " + catName
        self.alertText.set(alertTxt)


    def createRecExp(self):
        memo = self.recExpMemo.get()
        alertTxt = "Le text: " + memo
        self.alertText.set(alertTxt)


    def cancelRecExp(self):
        memo = self.cancelExpMemo.get()
        alertTxt = "Le text: " + memo
        self.alertText.set(alertTxt)


    def createRecInc(self):
        memo = self.recIncMemo.get()
        alertTxt = "Le text: " + memo
        self.alertText.set(alertTxt)


    def cancelRecInc(self):
        memo = self.cancelIncMemo.get()
        alertTxt = "Le text: " + memo
        self.alertText.set(alertTxt)


    def reportSingleInc(self):
        memo = self.singleIncMemo.get()
        alertTxt = "Le text: " + memo
        self.alertText.set(alertTxt)


if __name__ == "__main__":
    app = GUI(None, [("Hentai", 5000.00, 200.69), ("Johnny Sins Videos", 10000.00, 0.99)])
    app.title("STI Expense Tracker")
    app.mainloop()
