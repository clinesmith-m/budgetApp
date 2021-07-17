import tkinter as tk
import tkinter.ttk as ttk
from socket import *
import struct
import time
import traceback

class GUI(tk.Frame):
    def __init__(self, parent):
        self.setFrame(parent)
        self.parent = parent
        self.categories = []
        self.monthlyExps = []
        self.monthlyIncs = []
        self.setInteractions()
        # Optimizing by breaking all of the visual elements that need to be 
        # queried for into seperate catagories so they can be updated separately
        self.catLogs = []
        self.setCatLogs(constructorCall=True)
        self.recExpLogs = []
        self.setRecExpLogs(constructorCall=True)
        self.recIncLogs = []
        self.setRecIncLogs(constructorCall=True)
        self.statLogs = []
        self.setStatLogs(constructorCall=True)


    # All the necessary netcode helper functions

    # Initializes new connection with the backend
    def makeConn(self):
        try:
            sock = socket(AF_INET, SOCK_STREAM)
            sock.connect( ("127.0.0.1", 45601) )
            return sock
        except:
            print("Connection to backend failed")
            exit(1)


    # Receives and returns 4 byte big-endian int
    def recvInt(self, conn, intLen=4):
        data = b''
        while (len(data) < intLen):
            retVal = conn.recv(intLen - len(data))
            data += retVal
            if len(retVal) == 0:
                break

        num = int.from_bytes(data, byteorder="big", signed=False)
        return num


    # Receives a double and returns it
    def recvDouble(self, conn):
        data = b''
        while (len(data) < 8):
            retVal = conn.recv(8 - len(data))
            data += retVal
            if len(retVal) == 0:
                break

        doub = struct.unpack("!d", data)
        doub = round(doub[0], 2)
        return doub

    # Receives a string up to a newline, and returns the string
    def recvLine(self, conn):
        msg = b''

        # Getting a two-byte int representing the string's length
        strLen = self.recvInt(conn, 2)

        while len(msg) < strLen:
            retVal = conn.recv(strLen - len(msg))
            msg += retVal
            if len(retVal) == 0:
                return 0

        return msg.decode()


    # Sends variable length big-endian int
    def sendInt(self, conn, rawNum, intLen=4):
        num = int.to_bytes(rawNum, byteorder="big", signed=True, length=intLen)
        conn.send(num)


    # Sends an 8-byte double
    def sendDouble(self, conn, rawFloat):
        data = bytearray(struct.pack("!d", rawFloat))
        conn.send(data)


    # Sends a string that ends with a newline
    def sendLine(self, conn, msg):
        self.sendInt(conn, len(msg), 2)
        conn.send(msg.encode())


    # Updating all of the spending categories
    def updateCategories(self):
        self.categories = []

        updateSock = self.makeConn()
        updateSock.send("GCAT".encode())

        # Grabbing the individual records
        numCats = self.recvInt(updateSock)
        for i in range(0, numCats):
            catName = self.recvLine(updateSock)
            budgeted = self.recvDouble(updateSock)
            spent = self.recvDouble(updateSock)
            self.categories.append( (catName, budgeted, spent) )

        # Sending confirmation and closing the socket
        updateSock.send("T".encode())
        updateSock.close()


    # Updating the list of monthly expenses
    def updateMonthlyExps(self):
        self.monthlyExps = []

        updateSock = self.makeConn()
        updateSock.send("GMNE".encode())

        # Grabbing the individual records
        numExps = self.recvInt(updateSock)
        for i in range(0, numExps):
            memo = self.recvLine(updateSock)
            amount = self.recvDouble(updateSock)
            self.monthlyExps.append( (memo, amount) )

        # Sending confirmation and closing the socket
        updateSock.send("T".encode())
        updateSock.close()


    # Updating the list of monthly expenses
    def updateMonthlyIncs(self):
        self.monthlyIncs = []

        updateSock = self.makeConn()
        updateSock.send("GMNI".encode())

        # Grabbing the individual records
        numIncs = self.recvInt(updateSock)
        for i in range(0, numIncs):
            memo = self.recvLine(updateSock)
            amount = self.recvDouble(updateSock)
            self.monthlyIncs.append( (memo, amount) )

        # Sending confirmation and closing the socket
        updateSock.send("T".encode())
        updateSock.close()


    # Actually creating the GUI

    # Setting up the frame
    def setFrame(self, parent):
        # Creating a mainframe to hold the canvas and the scrollbar
        self.mainframe = tk.Frame(parent)
        self.mainframe.pack(fill="both", expand=True)
        # Making and packing the canvas and scrollbar
        self.canvas = tk.Canvas(self.mainframe)
        self.canvas.pack(side="left", fill="both", expand=True)
        self.vsb = ttk.Scrollbar(self.mainframe, orient="vertical", command=self.canvas.yview)
        self.vsb.pack(side="right", fill="y")
        # Configuring the canvas
        self.canvas.configure(yscrollcommand=self.vsb.set)
        self.canvas.bind(
            '<Configure>', 
            lambda e: self.canvas.configure(
                scrollregion=self.canvas.bbox("all")
            )
        )

        # Making the frame that actually holds all the data, and making it a
        # child of self.canvas
        self.frame = tk.Frame(self.canvas)
        self.canvas.create_window(
            (0, 0),
            window=self.frame,
            height=1200,
            width=960,
            anchor="nw",
            tags="self.frame"
        )

        self.frame.columnconfigure(0, weight=1)
        self.frame.rowconfigure(0, weight=1)


    # Creating the visual elements for the spending categories
    def setCatLogs(self, hasNewData=True, constructorCall=False):
        # Killing the widgets for the previous logs
        for widget in self.catLogs:
            widget.destroy()
        self.catLogs = []

        # Placing an overarching label over all the catLogs
        catIndex = 1
        sectionLabel = tk.Label(self.frame, text="Spending Categories", width=32)
        sectionLabel.grid(row=catIndex, column=5, pady=5, padx=5)
        self.catLogs.append(sectionLabel)

        # Adding the updated list of categories if necessary
        if hasNewData:
            self.updateCategories()

        # Displaying all the budget categories
        catIndex += 1
        for cat in self.categories:
            labelText = cat[0] + "\n" + str(round(cat[1], 2))\
                            + "\n" + str(round(cat[2], 2)) 
            currLabel = tk.Label(
                self.frame,
                text=labelText, 
                background="white", 
                relief="raised", 
                width=32
            )
            currLabel.grid(row=catIndex, column=5, pady=5, padx=8)
            self.catLogs.append(currLabel)
            catIndex += 1

        # Resetting the sections that come below, unless this is the initial
        # construction of the object
        if not constructorCall:
            self.setRecExpLogs(hasNewData=False)


    def setRecExpLogs(self, hasNewData=True, constructorCall=False):
        # Killing the widgets for the previous logs
        for widget in self.recExpLogs:
            widget.destroy()
        self.recExpLogs = []

        # Placing an overarching label over all the catLogs
        yIndex = len(self.catLogs) + 1
        sectionLabel = tk.Label(self.frame, text="Monthly Expenses", width=32)
        sectionLabel.grid(row=yIndex, column=5, pady=5, padx=5)
        self.recExpLogs.append(sectionLabel)

        # Adding the updated list of categories if necessary
        if hasNewData:
            self.updateMonthlyExps()

        # Displaying all the budget categories
        yIndex += 1
        for exp in self.monthlyExps:
            labelText = exp[0] + "\n" + str(round(exp[1], 2))
            currLabel = tk.Label(
                self.frame,
                text=labelText, 
                background="white", 
                relief="raised", 
                width=32
            )
            currLabel.grid(row=yIndex, column=5, pady=5, padx=8)
            self.recExpLogs.append(currLabel)
            yIndex += 1

        # Resetting the sections that come below, unless this is the initial
        # construction of the object
        if not constructorCall:
            self.setRecIncLogs(hasNewData=False)


    def setRecIncLogs(self, hasNewData=True, constructorCall=False):
        # Killing the widgets for the previous logs
        for widget in self.recIncLogs:
            widget.destroy()
        self.recIncLogs = []

        # Placing an overarching label over all the catLogs
        yIndex = len(self.catLogs) + len(self.recExpLogs) + 1
        sectionLabel = tk.Label(self.frame, text="Monthly Income Streams", width=32)
        sectionLabel.grid(row=yIndex, column=5, pady=5, padx=5)
        self.recIncLogs.append(sectionLabel)

        # Adding the updated list of categories if necessary
        if hasNewData:
            self.updateMonthlyIncs()

        # Displaying all the budget categories
        yIndex += 1
        for inc in self.monthlyIncs:
            labelText = inc[0] + "\n" + str(round(inc[1], 2))
            currLabel = tk.Label(
                self.frame,
                text=labelText, 
                background="white", 
                relief="raised", 
                width=32
            )
            currLabel.grid(row=yIndex, column=5, pady=5, padx=8)
            self.recIncLogs.append(currLabel)
            yIndex += 1

        # Resetting the sections that come below, unless this is the initial
        # construction of the object
        if not constructorCall:
            self.setStatLogs()


    # Setting up stat logs.
    def setStatLogs(self, constructorCall=False):
        # Getting data if this is a constructor call. If it isn't this'll never
        # need to update its data
        if constructorCall:
            # Getting data for the previous month
            statSock = self.makeConn()
            statSock.send("GPML".encode())
            prevMonthInc = self.recvDouble(statSock)
            prevMonthExp = self.recvDouble(statSock)
            prevMonthSaved = prevMonthInc - prevMonthExp
            statSock.send("T".encode())
            statSock.close()

            # The current year
            statSock = self.makeConn()
            statSock.send("GCYL".encode())
            currYearInc = self.recvDouble(statSock)
            currYearExp = self.recvDouble(statSock)
            currYearSaved = currYearInc - currYearExp
            statSock.send("T".encode())
            statSock.close()

            # And all time
            statSock = self.makeConn()
            statSock.send("GATL".encode())
            allTimeInc = self.recvDouble(statSock)
            allTimeExp = self.recvDouble(statSock)
            allTimeSaved = allTimeInc - allTimeExp
            statSock.send("T".encode())
            statSock.close()

            # Creating widgets for each set of logs, plus one for the overarching
            # label
            sectionLabelText = "Past Stats"
            sectionLabel = tk.Label(self.frame, text=sectionLabelText, width=32)
            self.statLogs.append(sectionLabel)

            prevMonthText = "Last Month\nIncome: " + str(round(prevMonthInc, 2)) + "\n"
            prevMonthText += "Expenses: " + str(round(prevMonthExp, 2)) + " "
            prevMonthText += "Saved: " + str(round(prevMonthSaved, 2))
            prevMonthLabel = tk.Label(
                self.frame,
                text=prevMonthText,
                background="white", 
                relief="raised", 
                width=32
            )
            self.statLogs.append(prevMonthLabel)

            currYearText = "This Year\nIncome: " + str(round(currYearInc, 2)) + "\n"
            currYearText += "Expenses: " + str(round(currYearExp, 2)) + " "
            currYearText += "Saved: " + str(round(currYearSaved, 2))
            currYearLabel = tk.Label(
                self.frame,
                text=currYearText,
                background="white", 
                relief="raised", 
                width=32
            )
            self.statLogs.append(currYearLabel)

            allTimeText = "All Time\nIncome: " + str(round(allTimeInc, 2)) + "\n"
            allTimeText += "Expenses: " + str(round(allTimeExp, 2)) + " "
            allTimeText += "Saved: " + str(round(allTimeSaved, 2))
            allTimeLabel = tk.Label(
                self.frame,
                text=allTimeText,
                background="white", 
                relief="raised", 
                width=32
            )
            self.statLogs.append(allTimeLabel)

        # Setting the correct y index and putting all the labels on the grid
        yIndex = len(self.catLogs) + len(self.recExpLogs) + len(self.recIncLogs) + 1
        for widget in self.statLogs:
            widget.grid(row=yIndex, column=5, pady=5, padx=8)
            yIndex += 1


    # Updating a single category and redrawing the associated widget
    def reDrawCat(self, catName):
        # Getting the tuple for the category
        catIndex = None
        for i in range(0, len(self.categories)):
            if catName == self.categories[i][0]:
                catIndex = i
                break

        # Getting the category from the backend
        sock = self.makeConn()
        sock.send("GCBN".encode())
        self.sendLine(sock, catName)
        budgeted = self.recvDouble(sock)
        spent = self.recvDouble(sock)

        # Updating the category tuple
        self.categories[catIndex] = (catName, budgeted, spent)

        # Sending confirmation and closing the socket
        sock.send("T".encode())
        sock.close()

        # Killing and redrawing the widget
        widgetIndex = catIndex + 1
        self.catLogs[widgetIndex].destroy()

        labelText = self.categories[catIndex][0] + "\n"
        labelText += str(round(self.categories[catIndex][1], 2)) + "\n" 
        labelText += str(round(self.categories[catIndex][2], 2))

        currLabel = tk.Label(
            self.frame,
            text=labelText, 
            background="white", 
            relief="raised", 
            width=32
        )

        currLabel.grid(row=widgetIndex+1, column=5, pady=5, padx=8)
        self.catLogs[widgetIndex] = currLabel        


    def setInteractions(self):
        # Creating the entry boxes for actual interactions

        # Starting with the Create new category field
        currRow=1
        tk.Label(
            self.frame,
            text="Create/Modify Category",
            width=32
        ).grid(row=currRow, column=1, padx=5)

        tk.Label(
            self.frame,
            text="Cat_Name-Amt",
            width=24
        ).grid(row=currRow, column=2, padx=5)

        currRow += 1
        self.catNameBox = tk.Entry(self.frame, width=32)
        self.catNameBox.grid(row=currRow, column=1, pady=5, padx=5)

        self.catAmtBox = tk.Entry(self.frame, width=10)
        self.catAmtBox.grid(row=currRow, column=2, pady=5, padx=5)

        self.modCatBtn = tk.Button(self.frame, text="Modify", command=self.modCategory)
        self.modCatBtn.grid(row=currRow, column=3, pady=5, padx=5)

        self.addCatBtn = tk.Button(self.frame, text="Add", command=self.addCategory)
        self.addCatBtn.grid(row=currRow, column=4, pady=5, padx=5)

        # Allowing user to report expenditures
        # One time expenditures first
        currRow += 1
        tk.Label(
            self.frame,
            text="Report One Time Expenditure",
            width=32
        ).grid(row=currRow, column=1, padx=5)

        tk.Label(
            self.frame,
            text="Cat_Name-Amt-Memo",
            width=24
        ).grid(row=currRow, column=2, padx=5)

        currRow += 1
        self.singleExpCat = tk.Entry(self.frame, width=32)
        self.singleExpCat.grid(row=currRow, column=1, pady=5, padx=5)

        self.singleExpAmt = tk.Entry(self.frame, width=10)
        self.singleExpAmt.grid(row=currRow, column=2, pady=5, padx=5)

        self.singleExpMemo = tk.Entry(self.frame, width=32)
        self.singleExpMemo.grid(row=currRow, column=3, pady=5, padx=5)

        self.singleExpBtn = tk.Button(self.frame, text="Report", command=self.reportSingleExp)
        self.singleExpBtn.grid(row=currRow, column=4, pady=5, padx=5)

        # Then recurring expenditures
        # To greatly simplify the design, recurring expenditures will be categories
        # unto themselves, rather than slotting into existing categories
        currRow += 1
        tk.Label(
            self.frame,
            text="Create Monthly Expenditure",
            width=32
        ).grid(row=currRow, column=1, padx=5)

        tk.Label(
            self.frame,
            text="Memo-Amt-Num_Months",
            width=24
        ).grid(row=currRow, column=2, padx=5)

        currRow += 1
        self.recExpMemo = tk.Entry(self.frame, width=32)
        self.recExpMemo.grid(row=currRow, column=1, pady=5, padx=5)

        self.recExpAmt = tk.Entry(self.frame, width=10)
        self.recExpAmt.grid(row=currRow, column=2, pady=5, padx=5)

        self.recExpLen = tk.Entry(self.frame, width=4)
        self.recExpLen.grid(row=currRow, column=3, pady=5, padx=5)

        self.recExpBtn = tk.Button(self.frame, text="Create", command=self.createRecExp)
        self.recExpBtn.grid(row=currRow, column=4, pady=5, padx=5)

        # Allowing user to cancel a monthly expense
        currRow += 1
        tk.Label(
            self.frame,
            text="Cancel Monthly Expense",
            width=32
        ).grid(row=currRow, column=1, padx=5)

        tk.Label(
            self.frame,
            text="Memo",
            width=24
        ).grid(row=currRow, column=2, padx=5)

        currRow += 1
        self.cancelExpMemo = tk.Entry(self.frame, width=32)
        self.cancelExpMemo.grid(row=currRow, column=1, pady=5, padx=5)

        self.cancelExpBtn = tk.Button(self.frame, text="Cancel", command=self.cancelRecExp)
        self.cancelExpBtn.grid(row=currRow, column=4, pady=5, padx=5)

        # Allowing user to create a monthly income
        currRow += 1
        tk.Label(
            self.frame,
            text="Create Monthly Income",
            width=32
        ).grid(row=currRow, column=1, padx=5)

        tk.Label(
            self.frame,
            text="Memo-Amt",
            width=24
        ).grid(row=currRow, column=2, padx=5)

        currRow += 1
        self.recIncMemo = tk.Entry(self.frame, width=32)
        self.recIncMemo.grid(row=currRow, column=1, pady=5, padx=5)

        self.recIncAmt = tk.Entry(self.frame, width=10)
        self.recIncAmt.grid(row=currRow, column=2, pady=5, padx=5)

        self.recIncBtn = tk.Button(self.frame, text="Create", command=self.createRecInc)
        self.recIncBtn.grid(row=currRow, column=4, pady=5, padx=5)

        # And cancel monthly income
        currRow += 1
        tk.Label(
            self.frame,
            text="Cancel Monthly Income",
            width=32
        ).grid(row=currRow, column=1, padx=5)

        tk.Label(
            self.frame,
            text="Memo",
            width=24
        ).grid(row=currRow, column=2, padx=5)

        currRow += 1
        self.cancelIncMemo = tk.Entry(self.frame, width=32)
        self.cancelIncMemo.grid(row=currRow, column=1, pady=5, padx=5)

        self.cancelIncBtn = tk.Button(self.frame, text="Cancel", command=self.cancelRecInc)
        self.cancelIncBtn.grid(row=currRow, column=4, pady=5, padx=5)

        # Also letting the user report one-time income
        currRow += 1
        tk.Label(
            self.frame, 
            text="Report One Time Income",
            width=32
        ).grid(row=currRow, column=1, padx=5)

        tk.Label(
            self.frame, 
            text="Memo-Amt",
            width=24
        ).grid(row=currRow, column=2, padx=5)

        currRow += 1
        self.singleIncMemo = tk.Entry(self.frame, width=32)
        self.singleIncMemo.grid(row=currRow, column=1, pady=5, padx=5)

        self.singleIncAmt = tk.Entry(self.frame, width=10)
        self.singleIncAmt.grid(row=currRow, column=2, pady=5, padx=5)

        self.singleIncBtn = tk.Button(self.frame, text="Report", command=self.reportSingleInc)
        self.singleIncBtn.grid(row=currRow, column=4, pady=5, padx=5)


        # Making an alert box at the bottom of the app to notify users of
        # changes in state
        currRow += 1
        self.alertText = tk.StringVar()
        alertBox = tk.Label(
            self.frame,
            textvariable=self.alertText, 
            width=32
        )
        alertBox.grid(row=currRow, column=1, pady=5, padx=5)


    def modCategory(self):
        catName = self.catNameBox.get()
        try:
            catAmt = float(self.catAmtBox.get())
        except:
            self.alertText.set("Invalid amount")
            return

        # Checking for invalid category names
        if catName == "":
            self.alertText.set("Name field left blank")

        found = False
        for cat in self.categories:
            if catName == cat[0]:
                found = True
                break
        if not found:
            self.alertText.set("Category doesn't exist")

        # Modifying the category
        modSock = self.makeConn()
        modSock.send("MCAT".encode())
        self.sendLine(modSock, catName)
        self.sendDouble(modSock, catAmt)
        modSock.recv(1)
        modSock.close()

        # Updating the interface
        self.reDrawCat(catName)
        self.alertText.set("Category modified")


    def addCategory(self):
        catName = self.catNameBox.get()
        try:
            catAmount = float(self.catAmtBox.get())
        except:
            self.alertText.set("Invalid amount")
            return

        # Checking for invalid category names
        if catName == "":
            self.alertText.set("Name field left blank")

        found = False
        for cat in self.categories:
            if catName == cat[0]:
                found = True
                break
        if found:
            self.alertText.set("Category already exists")

        # Adding the category
        addSock = self.makeConn()
        addSock.send("ACAT".encode())
        self.sendLine(addSock, catName)
        self.sendDouble(addSock, catAmount)
        self.alertText.set("Category added")
        addSock.recv(1)
        addSock.close()

        # Updating the interface
        self.setCatLogs()


    def reportSingleExp(self):
        # Getting the values from all the entry boxes
        catName = self.singleExpCat.get()
        memo = self.singleExpMemo.get()
        amt = self.singleExpAmt.get()

        # Checking for invalid entries

        # Starting with the category name
        if catName == "":
            self.alertText.set("Category field left blank")
            return

        found = False
        for cat in self.categories:
            if catName == cat[0]:
                found = True
                break

        if not found:
            self.alertText.set("Category doesn't exist")
            return

        # Then the memo
        if memo == "":
            # Sending a default memo instead of an empty string, just because I
            # don't want to mess with sending and empty string through a socket
            memo = "No memo needed"

        elif len(memo) > 64:
            self.alertText.set("Memo is too long")
            return

        # And lastly the amount
        try:
            amt = float(amt)
        except:
            self.alertText.set("Invalid amount")
            return

        # Finally, sending the info to the backend
        sock = self.makeConn()
        sock.send("LOTE".encode())
        self.sendLine(sock, catName)
        self.sendLine(sock, memo)
        self.sendDouble(sock, amt)
        sock.recv(1)
        sock.close()

        # Updating the interface
        self.reDrawCat(catName)

        self.alertText.set("Transaction Logged")


    def createRecExp(self):
        memo = self.recExpMemo.get()

        # Checking for invalid amounts
        try:
            expAmount = float(self.recExpAmt.get())
        except:
            self.alertText.set("Invalid Amount")
            return

        # Checking for invalid memos
        if memo == "":
            self.alertText.set("Memo Field Left Blank")
            return

        found = False
        for exp in self.monthlyExps:
            if memo == exp[0]:
                found = True
                break

        if found:
            self.alertText.set("Monthly expense already exists")
            return

        if len(memo) > 32:
            self.alertText.set("Memo is too long")
            return

        # Checking for invalid payment periods and setting it to -1 by default
        # if the field is left blank
        expLen = self.recExpLen.get()
        if expLen == "":
            expLen = -1
        else:
            try:
                expLen = int(expLen)
            except:
                self.alertText.set("Invalid payment time period")

        # Communicating with the backend
        sock = self.makeConn()
        sock.send("AMNE".encode())
        self.sendLine(sock, memo)
        self.sendDouble(sock, expAmount)
        self.sendInt(sock, expLen)
        sock.recv(1)
        sock.close()

        # Updating the interface
        self.alertText.set("Monthly Expense Added")
        self.setRecExpLogs()


    def cancelRecExp(self):
        memo = self.cancelExpMemo.get()

        # Checking for invalid memos
        if memo == "":
            self.alertText.set("Memo Field Left Blank")
            return

        found = False
        for exp in self.monthlyExps:
            if memo == exp[0]:
                found = True
                break

        if not found:
            self.alertText.set("Monthly expense doesn't exist")
            return

        # Communicating with the backend
        sock = self.makeConn()
        sock.send("CMNE".encode())
        self.sendLine(sock, memo)
        sock.recv(1)
        sock.close()

        # Updating the interface
        self.alertText.set("Monthly Expense Cancelled")
        self.setRecExpLogs()


    def createRecInc(self):
        memo = self.recIncMemo.get()

        # Checking for invalid amounts
        try:
            incAmount = float(self.recIncAmt.get())
        except:
            self.alertText.set("Invalid Amount")
            return

        # Checking for invalid memos
        if memo == "":
            self.alertText.set("Memo Field Left Blank")
            return

        found = False
        for inc in self.monthlyIncs:
            if memo == inc[0]:
                found = True
                break

        if found:
            self.alertText.set("Monthly income already exists")
            return

        if len(memo) > 32:
            self.alertText.set("Memo is too long")
            return

        # Communicating with the backend
        sock = self.makeConn()
        sock.send("AMNI".encode())
        self.sendLine(sock, memo)
        self.sendDouble(sock, incAmount)
        sock.recv(1)
        sock.close()

        # Updating the interface
        self.alertText.set("Monthly Income Added")
        self.setRecIncLogs()


    def cancelRecInc(self):
        memo = self.cancelIncMemo.get()

        # Checking for invalid memos
        if memo == "":
            self.alertText.set("Memo Field Left Blank")
            return

        found = False
        for inc in self.monthlyIncs:
            if memo == inc[0]:
                found = True
                break

        if not found:
            self.alertText.set("Monthly income doesn't exist")
            return

        # Communicating with the backend
        sock = self.makeConn()
        sock.send("CMNI".encode())
        self.sendLine(sock, memo)
        sock.recv(1)
        sock.close()

        # Updating the interface
        self.alertText.set("Monthly Income Cancelled")
        self.setRecIncLogs()


    def reportSingleInc(self):
        memo = self.singleIncMemo.get()
        amt = self.singleIncAmt.get()

        # Checking for invalid entries

        # Starting with the memo
        if memo == "":
            self.alertText.set("Memo field left blank")
            return

        if len(memo) > 64:
            self.alertText.set("Memo is too long")
            return

        # Then the amount
        try:
            amt = float(amt)
        except:
            self.alertText.set("Invalid Amount")
            return

        # Sending the event to the backend
        sock = self.makeConn()
        sock.send("LOTI".encode())
        self.sendLine(sock, memo)
        self.sendDouble(sock, amt)
        sock.recv(1)
        sock.close()

        # Updating the interface
        self.alertText.set("Income Logged")


if __name__ == "__main__":
    # The sleep statement makes sure the java backend has enough time to start
    # listening for connections.
    # This is dumb, bad, and potentially inconsistent, but this is a personal
    # project and it works, so...
    time.sleep(2)

    # The try/except is for debugging purposes only
    try:
        # This stuff is fine
        root_window = tk.Tk()
        app = GUI(root_window)
        root_window.title("STI Expense Tracker") # For the most part
        root_window.geometry("980x800")
        root_window.mainloop()
    except:
        traceback.print_exc()

    # Telling the Java backend to shut down
    sock = socket(AF_INET, SOCK_STREAM)
    sock.connect( ("127.0.0.1", 45601) )
    sock.send("DISC".encode())
    sock.close()
